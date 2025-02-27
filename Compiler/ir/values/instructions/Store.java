package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.operands.MipsOperand;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 写内存Store指令
 * store <ty> <value>, <ty>* <pointer>
 * @date 2024/11/18 20:48
 */
public class Store extends Instruction {
    // 构造函数：value->要存储的值，pointer->存储位置指针
    public Store(String name, BasicBlock parent, Value value, Value pointer) {
        super(name, new VoidType(), parent, value, pointer);
    }

    // store i32 %i2, i32* %i1
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("store ");
        // "i32 %i2, i32* %i1"
        IrUtils.appendSBParamList(sb, getOperands());
        return sb.toString();
    }

    public void buildMips() {
        MipsOperand src = MipsBuilder.buildOperand(getOp(1), false, MipsContext.curIrFunction, getParent());
        MipsOperand dstBase = MipsBuilder.buildOperand(getOp(2), false, MipsContext.curIrFunction, getParent());
        MipsOperand offset = MipsBuilder.buildImmOperand(0, true, MipsContext.curIrFunction, getParent());
        MipsBuilder.buildStore(src, dstBase, offset, getParent());
    }
}
