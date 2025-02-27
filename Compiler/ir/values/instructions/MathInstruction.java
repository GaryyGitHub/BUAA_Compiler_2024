package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 四则运算+取余指令的父类，共用一套构造函数和toString()方法
 * @date 2024/11/20 19:55
 */
public class MathInstruction extends Instruction {
    public MathInstruction(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, new IntType(32), parent, op1, op2);
    }

    // 形如：%i7 = add i32 %i4, %i6
    // 这里"i32 %i4"写在一起，使用typeAndNameStr()方法拼接
    public String toMathInstructionString(String instName) {
        return getName() + " = " + instName + " " +
                IrUtils.typeAndNameStr(getOperands().get(0)) + ", " +
                getOperands().get(1).getName();
    }
}
