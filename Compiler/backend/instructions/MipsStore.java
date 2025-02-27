package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @author Gary
 * @Description: Mips中的Store指令. 这个指令很特殊，有3个操作数，但没有dst
 * sw $s1, 0($s0)
 * @date 2024/12/7 16:37
 */
public class MipsStore extends MipsInstruction {
    // store指令的3个操作数都是use，而没有def
    public MipsStore(MipsOperand src, MipsOperand dstBase, MipsOperand offset) {
        super(src, dstBase, offset, true);
    }

    // src: 1, offset: 3, dstBase: 2
    public String toString() {
        return "sw\t" + getSrc(1) + ",\t" + getSrc(3) + "(" + getSrc(2) +")\n";
    }
}
