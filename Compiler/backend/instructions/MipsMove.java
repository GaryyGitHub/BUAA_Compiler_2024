package backend.instructions;

import backend.operands.MipsImm;
import backend.operands.MipsLabel;
import backend.operands.MipsOperand;

/**
 * @author Gary
 * @Description: move指令：move $dst, $src
 * @date 2024/12/5 18:05
 */
public class MipsMove extends MipsInstruction {
    public MipsMove(MipsOperand dst, MipsOperand src) {
        super(dst, src);
    }

    public String toString() {
        // 1. 立即数的move：使用li指令
        if (getSrc(1) instanceof MipsImm) {
            return "li\t" + getDst() + ",\t" + getSrc(1) + "\n";
        }
        // 2. 全局变量：使用la，加载响应标签对应的地址
        else if (getSrc(1) instanceof MipsLabel) {
            return "la\t" + getDst() + ",\t" + getSrc(1) + "\n";
        }
        // 3. 寄存器：使用move指令
        else {
            return "move\t" + getDst() + ",\t" + getSrc(1) + "\n";
        }
    }
}
