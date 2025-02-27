package backend.instructions;

import backend.reg.Reg;
import backend.units.MipsFunction;

/**
 * @author Gary
 * @Description: Mips 返回指令，主要逻辑其实在toString函数中
 * @date 2024/12/8 13:58
 */
public class MipsRet extends MipsInstruction {
    // 所属函数
    private MipsFunction function;

    public MipsRet(MipsFunction function) {
        this.function = function;
    }

    // 完成了非主函数的恢复现场。需要注意：并没有将lw和jr加入指令序列
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 1. 函数返回前，需要把$sp恢复原位，与MipsFunction的移动$sp对应
        int totalStackSize = function.getTotalStackSize();
        if (totalStackSize > 0) {
            sb.append("add\t$sp,\t$sp,\t").append(totalStackSize).append("\n");
        }
        // 2. 分类讨论，构建return时的指令
        // 2.1 主函数：结束运行
        if (function.getName().equals("main")) {
            sb.append("\tli\t$v0,\t10\n");
            sb.append("\tsyscall\n\n");
        }
        // 2.2 其他函数：要恢复现场
        // 具体操作：把需要保存的寄存器的值从栈中恢复到寄存器中
        // 然后跳转到返回地址
        else {
            // 恢复寄存器，和MipsFunction的保存寄存器对应, lw $reg, offset($sp)
            int stackOffset = -4;
            for (Reg reg : function.getRegs2Save()) {
                sb.append("\tlw\t").append(reg).append(",\t").append(stackOffset).append("($sp)\n");
                stackOffset -= 4;
            }
            // jr $ra
            sb.append("\tjr\t$ra\n");
        }
        return sb.toString();
    }
}
