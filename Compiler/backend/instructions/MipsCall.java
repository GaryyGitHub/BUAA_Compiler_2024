package backend.instructions;

import backend.units.MipsFunction;

/**
 * @author Gary
 * @Description: Mips Call(jal)指令
 * jal funcName
 * @date 2024/12/7 15:21
 */
public class MipsCall extends MipsInstruction {
    // 调用函数
    private MipsFunction function;

    public MipsCall(MipsFunction function) {
        this.function = function;
    }

    public String toString() {
        return "jal\t" + function.getName() + "\n";
    }
}
