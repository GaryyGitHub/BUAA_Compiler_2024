package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsCondType;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrUtils;

/**
 * @author Gary
 * @Description:
 * <result> = icmp <cond> <ty> <op1>, <op2>
 * @date 2024/11/20 20:13
 */
public class Icmp extends Instruction {
    private CondType condType;

    public CondType getCondType() {
        return condType;
    }

    public Icmp(String name, BasicBlock parent, CondType condType, Value op1, Value op2) {
        super(name, new IntType(1), parent, op1, op2);
        this.condType = condType;
    }

    // 枚举一下可能的比较条件类型
    public enum CondType {
        EQL("eq"),  // ==
        NEQ("ne"),  // !=
        LEQ("sle"), // <=
        LSS("slt"), // <
        GEQ("sge"), // >=
        GRE("sgt"); // >
        private String irString;
        CondType(String irString) {
            this.irString = irString;
        }
        public String toString() {
            return this.irString;
        }
    }

    // 形如：%5 = icmp ne i32 0, %4
    public String toString() {
        return getName() + " = icmp " +         // "%5 = icmp"
                condType + " " +                // "ne "
                IrUtils.typeAndNameStr(getOperands().get(0)) + ", " +  // "i32 0, "
                getOperands().get(1).getName(); // "%4"
    }

    public void buildMips() {}

    /**
     * icmp指令得到的值都是i1类型，若需要zext，这里提供buildMips方法
     */
    public void buildMipsZext() {
        MipsCondType mipsCondType = MipsCondType.ir2MipsCondType(condType);
        Value op1 = getOp(1), op2 = getOp(2);
        // 1. op1和op2都是常数，直接进行比较，得到0或1的MipsImm结果，不用生成指令了
        if (op1 instanceof ConstInt && op2 instanceof ConstInt) {
            int op1Val = IrUtils.getConstIntValue(op1);
            int op2Val = IrUtils.getConstIntValue(op2);
            MipsOperand opRes = new MipsImm(MipsCondType.immCondCalc(mipsCondType, op1Val, op2Val));
            MipsContext.addOperandMap(this, opRes);
        }
        // 2. 二者之一不是常数，要生成b类指令
        else {
            MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());
            MipsOperand src1 = MipsBuilder.buildOperand(op1, false, MipsContext.curIrFunction, getParent());
            MipsOperand src2 = MipsBuilder.buildOperand(op2, false, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildCompare(mipsCondType, dst, src1, src2, getParent());
        }
    }
}
