package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 减法指令，也用在一元表达式中负号的处理
 * <result> = sub <ty> <op1>, <op2>
 * @date 2024/11/20 19:53
 */
public class Sub extends MathInstruction {
    public Sub(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    public String toString() {
        return toMathInstructionString("sub");
    }

    /**
     * 与add不同的是，op1如果是立即数，需要转化成寄存器，而add可以直接用立即数的形式进行addiu
     * 即：%i8 = sub i32 1, %i7 是不能用subiu的，只有op2是立即数时，才可以用subiu(我用的是addiu)
     */
    public void buildMips() {
        Value op1 = getOp(1), op2 = getOp(2);
        MipsOperand rs, rt;
        MipsOperand rd = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());

        // 1. op1和op2都是立即数，直接move dst op1-op2（直接没有addiu了）
        if (op1 instanceof ConstInt && op2 instanceof ConstInt) {
            int imm1 = IrUtils.getConstIntValue(op1);
            int imm2 = IrUtils.getConstIntValue(op2);
            MipsBuilder.buildMove(rd, new MipsImm(imm1 - imm2), getParent());
        }
        // 2. op2是立即数，则op1不是立即数，此时可以用addiu op1 + (-op2)
        else if (op2 instanceof ConstInt) {
            int imm2 = IrUtils.getConstIntValue(op2);
            rs = MipsBuilder.buildOperand(op1, false, MipsContext.curIrFunction, getParent());
            rt = MipsBuilder.buildImmOperand(-imm2, true, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.ADDU, rd, rs, rt, getParent());
        }
        // 3. op2不是立即数，用subu
        else {
            rs = MipsBuilder.buildOperand(op1, false, MipsContext.curIrFunction, getParent());
            rt = MipsBuilder.buildOperand(op2, true, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.SUBU, rd, rs, rt, getParent());
        }
    }
}
