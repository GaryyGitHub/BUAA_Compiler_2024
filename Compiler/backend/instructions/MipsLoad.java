package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @author Gary
 * @Description: load指令: lw $t1, offset($t2)
 * @date 2024/12/5 22:38
 */
public class MipsLoad extends MipsInstruction {
    /**
     * 构造函数
     * @param dst   目的寄存器
     * @param base  基址寄存器
     * @param offset    偏移量
     */
    public MipsLoad(MipsOperand dst, MipsOperand base, MipsOperand offset) {
        super(dst, base, offset);
    }

    // offset是第二个src，base是第一个src
    public String toString() {
        return "lw\t" + getDst() + ",\t" + getSrc(2) + "(" + getSrc(1) + ")\n";
    }
}
