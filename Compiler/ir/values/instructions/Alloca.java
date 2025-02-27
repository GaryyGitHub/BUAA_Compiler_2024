package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import backend.units.MipsFunction;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.constants.ConstArray;

/**
 * @author Gary
 * @Description: 内存申请指令，对应的Value应为指针类型
 * <result> = alloca <type>
 * @date 2024/11/18 17:27
 */
public class Alloca extends Instruction {
    // ============ 成员变量 ============
    private ConstArray initArray = null;    // 用于处理常量数组
    private ValueType allocatedType = null; // 要分配的类型

    public ConstArray getInitArray() {
        return initArray;
    }

    // ============ 构造函数 ============
    /**
     * 专门用来处理 没有操作数的指令
     * @param name          指令Value的名称
     * @param pointingType  要指向的类型
     * @param parent        所在基本块(一定是BasicBlock)
     */
    public Alloca(String name, ValueType pointingType, BasicBlock parent) {
        super(name, new PointerType(pointingType), parent);
        allocatedType = pointingType;
    }

    /**
     * 专门用来处理带有初值的数组常量
     * @param name          指令Value的名称
     * @param pointingType  要指向的类型
     * @param parent        所在基本块(一定是BasicBlock)
     * @param initArray     常量初值
     */
    public Alloca (String name, ValueType pointingType, BasicBlock parent, ConstArray initArray) {
        super(name, new PointerType(pointingType), parent);
        this.initArray = initArray;
        this.allocatedType = pointingType;
    }

    public String toString() {
        return getName() + " = alloca " + allocatedType;
    }

    public void buildMips() {
        MipsFunction curFunction = MipsContext.getFunction(MipsContext.curIrFunction);
        // 1. 获取$sp指针目前位置（栈上已经分配的空间），转换成imm操作数（后面addu的rt操作数）
        int spOffset = curFunction.getAllocaSize();
        MipsOperand immSpOffset = MipsBuilder.buildImmOperand(spOffset, true, MipsContext.curIrFunction, getParent());

        // 2. 在MIPS中分配栈空间
        int allocaSize = allocatedType.getSize(true);
        // 这个很重要！看看给sp分配多少空间！
//        System.out.println("allocaSize "+curFunction+" "+allocaSize);
        curFunction.addAllocaSize(allocaSize);

        // 3. 先构造rd,然后 addiu $s1, $sp, 4，令栈向上生长
        // 这里的rd是指令，在buildOperand中需要生成新的dst寄存器 FIXME: 这里的isImm没用吧
        MipsOperand rd = MipsBuilder.buildOperand(this, true, MipsContext.curIrFunction, getParent());
        MipsBuilder.buildBinary(MipsBinary.Type.ADDU, rd, MipsRReg.SP, immSpOffset, getParent());
    }
}
