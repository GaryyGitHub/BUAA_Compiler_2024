package backend.instructions;

import backend.operands.MipsOperand;
import backend.units.MipsBlock;

/**
 * @author Gary
 * @Description: b类指令，包含：
 * j target 跳转指令
 * b开头的分支指令
 * @date 2024/11/29 15:23
 */
public class MipsBranch extends MipsInstruction {
    /**
     * 跳转时需要比较的条件
     * 值为null - 无条件跳转指令
     * 其他情况：EQ NE LE LT GE GT - 条件跳转指令
     */
    private MipsCondType condType;

    // 要跳转到的MIPS块
    private MipsBlock target;

    /**
     * 无条件跳转指令: j target
     * @param target 跳转目标
     */
    public MipsBranch(MipsBlock target) {
        this.target = target;
        this.condType = null;
    }

    /**
     * 有条件跳转指令：b<condType> op1, op2, target
     * @param condType  条件类型
     * @param op1       条件左操作数
     * @param op2       条件右操作数
     * @param target    跳转目标
     */
    public MipsBranch(MipsCondType condType, MipsOperand op1, MipsOperand op2, MipsBlock target) {
        super(null, op1, op2);
        this.target = target;
        this.condType = condType;
    }

    public String toString() {
        if (getSrc(1) == null) {
            // 1. 没有src，说明是无条件跳转指令
            return "j\t" + target.getName() + "\n";
        } else {
            // 2. 有条件跳转指令，在枚举类的名字前面加个'b'即表示相应的指令
            // 很方便：condType的toString()方法就是对应的指令名
            return "b" + condType + "\t" + getSrc(1) + ",\t" + getSrc(2) + ",\t" + target.getName() + "\n";
        }
    }
}
