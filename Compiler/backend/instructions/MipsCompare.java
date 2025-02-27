package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @author Gary
 * @Description: s类指令
 * 例如：seq $t0, $t1, $t2  若 $t1 = $t2, 则 $t0 = 1, 否则 $t0 = 0
 * @date 2024/12/8 10:59
 */
public class MipsCompare extends MipsInstruction {
    private MipsCondType type;

    public MipsCompare(MipsCondType type, MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        super(dst, src1, src2);
        this.type = type;
    }

    // 在type枚举类的名字前面加个's'即表示相应的指令
    public String toString() {
        return "s" + type + "\t" + getDst() + ",\t" + getSrc(1) + ",\t" + getSrc(2) + "\n";
    }
}
