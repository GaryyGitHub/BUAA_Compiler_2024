package backend.instructions;

import ir.values.instructions.Icmp;

/**
 * @author Gary
 * @Description: 分支语句比较运算的类型枚举类
 * @date 2024/11/29 15:27
 */
public enum MipsCondType {
    EQ("==", "eq"),
    NE("!=", "ne"),
    LT("<", "lt"),
    LE("<=", "le"),
    GT(">", "gt"),
    GE(">=", "ge");

    private String name;
    private String meaning;
    MipsCondType(String meaning, String name) {
        this.meaning = meaning;
        this.name = name;
    }

    /**
     * 把Icmp.CondType枚举类型转换为MipsCondType枚举类型
     * @param type  Icmp.CondType枚举类型
     * @return      MipsCondType枚举类型
     */
    public static MipsCondType ir2MipsCondType(Icmp.CondType type) {
        return switch (type) {
            case EQL -> EQ;
            case NEQ -> NE;
            case LEQ -> LE;
            case LSS -> LT;
            case GEQ -> GE;
            case GRE -> GT;
        };
    }

    /**
     * 直接根据立即数值计算条件运算结果
     * @param type  MipsCondType枚举类型
     * @param op1   操作数1
     * @param op2   操作数2
     * @return      条件运算结果，为int类型
     */
    public static int immCondCalc(MipsCondType type, int op1, int op2) {
        boolean res = switch (type) {
            case EQ -> op1 == op2;
            case NE -> op1 != op2;
            case LE -> op1 <= op2;
            case LT -> op1 < op2;
            case GE -> op1 >= op2;
            case GT -> op1 > op2;
        };
        return res ? 1 : 0;
    }

    public String toString() {
        return name;
    }
}
