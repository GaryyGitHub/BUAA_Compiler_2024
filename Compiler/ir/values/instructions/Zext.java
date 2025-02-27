package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.operands.MipsOperand;
import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 无符号扩展，将i1或i8转换为i32
 * <result> = zext <ty> <value> to <ty2>
 * @date 2024/11/20 20:29
 */
public class Zext extends Instruction {
    public Zext(String name, BasicBlock parent, Value value) {
        super(name, new IntType(32), parent, value);
    }

    // %1 = zext i8 %2 to i32
    public String toString() {
        // "%1 = zext "
        return getName() + " = zext " +
                // "i8 %2"
                IrUtils.typeAndNameStr(getOperands().get(0)) +
                // " to i32" FIXME: 这里应该只可能是i32，不知道会不会是i8，待确认！
                " to i32";
    }

    public void buildMips() {
        Value value2Zext = getOp(1);
        System.out.println("遇到zext指令了！！value是：" + value2Zext + " 类型：" + value2Zext.getType());
        if (value2Zext instanceof Icmp) {
            ((Icmp) value2Zext).buildMipsZext();
        }
        // 无论value2Zext是i1还是i8，都需要map一下！
        MipsOperand src = MipsBuilder.buildOperand(value2Zext, true, MipsContext.curIrFunction, getParent());
        MipsOperand dst = MipsBuilder.buildOperand(this, true, MipsContext.curIrFunction, getParent());
//        MipsContext.addOperandMap(this, MipsContext.getOperand(value2Zext));
        MipsBuilder.buildMove(dst, src, getParent());
    }
}
