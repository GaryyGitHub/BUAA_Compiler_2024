package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: ret 指令
 * 1. ret <type> <value>
 * 2. ret void
 * @date 2024/11/19 22:28
 */
public class Ret extends Instruction {
    // 无返回值：ret void
    public Ret(BasicBlock parent) {
        super("", new VoidType(), parent);
    }

    // 有返回值：ret <type> <value>
    public Ret(BasicBlock parent, Value retVal) {
        super("", retVal.getType(), parent, retVal);
    }

    // ret i32 %1
    // ret void
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ret ");  // "ret "
        if (getType() instanceof VoidType) {
            sb.append("void");   // "void"
        } else {
            // "i32 %1"
            sb.append(IrUtils.typeAndNameStr(getOperands().get(0)));
        }
        return sb.toString();
    }

    public void buildMips() {
        Value retVal = getOp(1);
        // 若有返回值，则move到$v0寄存器
        if (retVal != null) {
            MipsOperand retValOp = MipsBuilder.buildOperand(retVal, true, MipsContext.curIrFunction, getParent());
            // move $v0, $s0
            MipsBuilder.buildMove(MipsRReg.V0, retValOp, getParent());
        }
        // 执行弹栈与返回操作，普通函数jr $ra，主函数要syscall
        MipsBuilder.buildRet(MipsContext.curIrFunction, getParent());
    }
}
