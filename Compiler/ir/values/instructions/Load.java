package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.operands.MipsOperand;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 读取内存指令
 * <result> = load <ty>, ptr <pointer>
 * @date 2024/11/20 17:07
 */
public class Load extends Instruction {
    // super中的ValueType是指针指向的类型，pointer是操作数，即指针的地址
    public Load(String name, BasicBlock parent, Value pointer) {
        super(name, IrUtils.getPointingTypeOfPointer(pointer), parent, pointer);
    }

    // %2 = load i32, i32* @c
    public String toString() {
        return getName() + " = load " +   // "%2 = load "
                getType() + ", " +        // "i32, "
                IrUtils.typeAndNameStr(getOperands().get(0));   // "i32* @c"
    }

    public void buildMips() {
        MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());
        MipsOperand base = MipsBuilder.buildOperand(getOp(1), false, MipsContext.curIrFunction, getParent());
        MipsOperand offset = MipsBuilder.buildImmOperand(0, true, MipsContext.curIrFunction, getParent());
        MipsBuilder.buildLoad(dst, base, offset, getParent());
    }
}
