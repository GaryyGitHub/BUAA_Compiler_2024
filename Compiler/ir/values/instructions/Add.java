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
 * @Description: add指令
 * <result> = add <ty> <op1>, <op2>
 * @date 2024/11/20 19:52
 */
public class Add extends MathInstruction{
    public Add(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    public String toString() {
        return toMathInstructionString("add");
    }

    // addu & addiu
    public void buildMips() {
        Value op1 = getOp(1), op2 = getOp(2);
        MipsOperand rs, rt;
        MipsOperand rd = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());

        // 1. op1和op2都是常数，直接move dst op1+op2（直接没有addiu了）
        if (op1 instanceof ConstInt && op2 instanceof ConstInt) {
            int imm1 = IrUtils.getConstIntValue(op1);
            int imm2 = IrUtils.getConstIntValue(op2);
            MipsBuilder.buildMove(rd, new MipsImm(imm1 + imm2), getParent());
        }
        // 2. op1是常数，op2不是，则需要把op1做为右操作数
        else if (op1 instanceof ConstInt) {
            rs = MipsBuilder.buildOperand(op2, false, MipsContext.curIrFunction, getParent());
            rt = MipsBuilder.buildOperand(op1, true, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.ADDU, rd, rs, rt, getParent());
        }
        // 3. op1不是常数
        else {
            rs = MipsBuilder.buildOperand(op1, false, MipsContext.curIrFunction, getParent());
            rt = MipsBuilder.buildOperand(op2, true, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.ADDU, rd, rs, rt, getParent());
        }
    }
}
