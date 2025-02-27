package backend.instructions;

import backend.operands.MipsImm;
import backend.operands.MipsOperand;

/**
 * @author Gary
 * @Description: Mips中的双操作数指令
 * @date 2024/12/7 11:03
 */
public class MipsBinary extends MipsInstruction {
    public enum Type {
        /**
         * 无符号整数加法：addu rd, rs, rt
         */
        ADDU("addu"),
        /**
         * 无符号整数减法：subu rd, rs, rt
         */
        SUBU("subu"),
        /**
         * 有符号整数乘法：mul rd, rs, rt
         */
        MUL("mul"),
        /**
         * 有符号整数除法：div rd, rs, rt
         */
        DIV("div"),
        /**
         * 如果无符号整数rs小于rt，则将目标寄存器设置为1，否则设置为0
         * <br>sltu rd, rs, rt
         */
        SLTU("sltu"),
        /**
         * 有符号整数乘法，并将乘积和目标寄存器的值累加
         * 本质是执行**mult**指令覆盖到HI，然后存入dst寄存器
         * <br>smmul rd, rs, rt. rd=rd+rs*rt
         */
        SMMUL("smmul"),
        /**
         * 有符号整数乘法，并把rd的值相加
         * <br>smmadd rd, rs, rt, ra. rd = rs*rt+ra
         */
        SMMADD("smmadd"),
        /**
         * 按位与：and rd, rs, rt
         * 专门给trunc指令使用
         */
        AND("and");

        public String name;
        Type(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }
    private Type type;
    public MipsBinary(Type type, MipsOperand rd, MipsOperand rs, MipsOperand rt) {
        super(rd, rs, rt);
        this.type = type;
    }

    public String toString() {
        // 1. 若rt为立即数，则应当选择带有i的指令：addiu
        if (getSrc(2) instanceof MipsImm) {
            String instruction = switch (type) {
                case ADDU -> "addiu";
                case SUBU -> "subiu";
                case SLTU -> "sltiu";
                default -> "" + type;
            };
            // addiu	$k1,	$sp,	0
            return instruction + "\t" + getDst() + ",\t" + getSrc(1) + ",\t" + getSrc(2) + "\n";
        }
        // 2. rt不是立即数，正常构建各类指令即可
        switch (type) {
            case DIV -> {
                // div $ra, $s0 -> mflo $s0
                return "div\t" + getSrc(1) + ",\t" + getSrc(2) + "\n\t" +
                        "mflo\t" + getDst() + "\n";
            }
            case SMMUL -> {
                // rd = rd+rs*rt
                return "mult\t" + getSrc(1) + ",\t" + getSrc(2) + "\n\t" +
                        "mfhi\t" + getDst() + "\n";
            }
            case SMMADD -> {
                // rd = rs*rt+ra
                return "madd\t" + getSrc(1) + ",\t" + getSrc(2) + "\n\t" +
                        "mfhi\t" + getDst() + "\n";
            }
            default -> {
                return type + "\t" + getDst() + ",\t" + getSrc(1) + ",\t" + getSrc(2) + "\n";
            }
        }
    }
}
