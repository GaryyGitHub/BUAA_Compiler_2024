package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 取余指令
 * <result> = srem <type> <op1>, <op2>
 * @date 2024/11/21 17:17
 */
public class Srem extends MathInstruction {
    public Srem(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    public String toString() {
        return toMathInstructionString("srem");
    }

    /**
     * 在MulExpNode.java中，已经规定：只有op2是常量且为1才srem
     * 此时，结果一定为0，只需要进行move即可！！
     */
    public void buildMips() {
        if (IrUtils.getConstIntValue(getOp(2)) != 1) {
            System.out.println("GaryError: Srem.buildMips()取余的右操作数不是1！错误");
        }
        MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());
        MipsBuilder.buildMove(dst, MipsRReg.ZERO, getParent());
    }
}
