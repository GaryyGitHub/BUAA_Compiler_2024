package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @author Gary
 * @Description: 除法指令
 * <result> = sdiv <ty> <op1>, <op2>
 * @date 2024/11/21 16:59
 */
public class Sdiv extends MathInstruction{
    public Sdiv(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    public String toString() {
        return toMathInstructionString("sdiv");
    }

    // FIXME: 先不考虑优化问题
    public void buildMips() {
        MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());
        MipsOperand src1 = MipsBuilder.buildOperand(getOp(1), false, MipsContext.curIrFunction, getParent());
        MipsOperand src2 = MipsBuilder.buildOperand(getOp(2), false, MipsContext.curIrFunction, getParent());
        MipsBuilder.buildBinary(MipsBinary.Type.DIV, dst, src1, src2, getParent());
    }
}
