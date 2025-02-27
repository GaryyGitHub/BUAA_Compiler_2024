package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import ir.IrBuilder;
import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 寻址指令，计算目标地址，对应value为指针类型
 * <result> = getelementptr <ty>, <ty>* <ptrval>, {<ty> <index>}*
 * @date 2024/11/18 19:26
 */
public class GetElementPtr extends Instruction {
    // 基地址指针ptrval指向的类型
    private ValueType ptrPointingType;
    /**
     * 双参数寻址指令，用于数组寻址. 所得的value类型为指针
     * 例如 ptrval为[3 * i32]*，那么返回的指针类型应该指向0维数组int，即i32*
     * @param name    指令名
     * @param parent  所属基本块
     * @param ptrval  基地址指针
     * @param index1  本维偏移
     * @param index2  高维偏移
     */
    public GetElementPtr(String name, BasicBlock parent, Value ptrval, Value index1, Value index2) {
        super(name,
                new PointerType(IrUtils.getElementTypeOfArrayPointer(ptrval)),
                parent, ptrval, index1, index2);
        this.ptrPointingType = IrUtils.getPointingTypeOfPointer(ptrval);
    }

    /**
     * 双参数寻址指令，所得value类型为指针，与传入ptrval类型一致. 实质为指针的加减
     * @param name   指令名
     * @param parent    所属基本块
     * @param ptrval    基地址指针. 当前instruction与基地址的类型相同
     * @param index1    本维偏移
     */
    public GetElementPtr(String name, BasicBlock parent, Value ptrval, Value index1) {
        super(name, ptrval.getType(), parent, ptrval, index1);
        this.ptrPointingType = IrUtils.getPointingTypeOfPointer(ptrval);
    }

    // 例子：
    // %1 = getelementptr [7 x i32], [7 x i32]* @a, i32 0, i32 3
    // %2 = getelementptr i32, i32* %3, i32 4

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = getelementptr ");
        sb.append(ptrPointingType).append(", ");   // 取出的类型
        IrUtils.appendSBParamList(sb, getOperands());   // "[7 x i32]* @a"
        return sb.toString();
    }

    // ============ MIPS相关 ============

    /**
     * 计算某一维的偏移量 FIXME: 有点迷惑
     * 本质上应该就是addu，一次是本维偏移，一次是高维偏移
     * @param dst    目的寄存器
     * @param temp   中转临时寄存器
     * @param base   基地址寄存器
     * @param irOffset   偏移量，Value类型
     * @param ptrPointingType    该维基本元素的类型
     * @param is1Dim    是否为一维偏移，即 opNum == 2
     */
    private void handleDim(MipsOperand dst, MipsOperand temp, MipsOperand base, Value irOffset, ValueType ptrPointingType, boolean is1Dim) {
        // 计算指针类型大小，一般为4. 数组就是4*size, 不过一般乘0
        int pointerSize = ptrPointingType.getSize(true);
        // 1. 偏移量是常量
        if (irOffset instanceof ConstInt) {
            int baseOffset = IrUtils.getConstIntValue(irOffset);
            // 偏移量
            int offset = baseOffset * pointerSize;
            // ptrPointingType.getBits()有三种情况：1. int: 32; 2. char: 8; 3. 其他，应该只有int/char数组：0
//            System.out.println("offset: " + offset +"="+baseOffset+"*"+pointerSize +", bits: "+ptrPointingType.getBits());

            // 1.1 没有偏移量，并且是一维偏移，只需要把this映射到base的op
            if (offset == 0 && is1Dim) {
                MipsContext.addOperandMap(this, base);
            } else {
                MipsOperand offsetOp = MipsBuilder.buildImmOperand(offset, true, MipsContext.curIrFunction, getParent());
                // dst = base + 本维偏移offset
                MipsBuilder.buildBinary(MipsBinary.Type.ADDU, dst, base, offsetOp, getParent());
            }
        }
        // 2. 偏移量是寄存器
        else {
            // 使用temp寄存器：temp = offset = irOffset * pointerSize
            MipsOperand irOffsetOp = MipsBuilder.buildOperand(irOffset, false, MipsContext.curIrFunction, getParent());
            MipsOperand pointerSizeOp = MipsBuilder.buildOperand(new ConstInt(32, pointerSize), false, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.MUL, temp, irOffsetOp, pointerSizeOp, getParent());
            // dst = base + temp
            MipsBuilder.buildBinary(MipsBinary.Type.ADDU, dst, base, temp, getParent());
        }
    }

    // 两维偏移就addiu两次
    public void buildMips() {
        // 1. 获取基地址(eg 数组基地址%i1)和本维偏移
        Value irBase = getOp(1);
        Value irOffset = getOp(2);
        // 2. 获得数组基地址的MipsOp，获取当前value指令的MipsOp作为目的寄存器
        MipsOperand base = MipsBuilder.buildOperand(irBase, false, MipsContext.curIrFunction, getParent());
        MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());
        // 3. 分类讨论：操作数的个数
        int opNum = getOperands().size();
        // 3.1 两个参数：基地址+本维偏移
        if (opNum == 2) {
            handleDim(dst, dst, base, irOffset, ptrPointingType, true);
        }
        // 3.2 三个参数：数组，基地址+本维偏移+高维偏移
        // %1 = getelementptr [7 x i32], [7 x i32]* @a, i32 0, i32 3
        else if (opNum == 3) {
            Value irHighOffset = getOp(3);
            // 数组**元素**类型：i32/i8
            ValueType elementType = ((ArrayType) ptrPointingType).getElementType();
            // 本维偏移(i32*/i8*), 0
            handleDim(dst, dst, base, irOffset, ptrPointingType, false);
            // 高维偏移(i32/i8), 3
            handleDim(dst, MipsRReg.AT, dst, irHighOffset, elementType, false);
        }
    }
}
