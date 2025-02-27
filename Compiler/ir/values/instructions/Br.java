package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsCondType;
import backend.operands.MipsOperand;
import backend.units.MipsBlock;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 跳转指令
 * 有条件跳转：br i1 <cond>, label <iftrue>, label <iffalse>
 * 无条件跳转：br label <dest>
 * @date 2024/11/20 1:13
 */
public class Br extends Instruction{
    // 标志是否为有条件跳转
    private boolean isConditional = false;

    /**
     * 无条件跳转指令
     * @param parent 一定是基本块
     * @param target 跳转到的目标基本块
     */
    public Br(BasicBlock parent, BasicBlock target) {
        super("", new VoidType(), parent, target);
        isConditional = false;
    }

    /**
     * 有条件跳转指令
     * @param cond          跳转条件
     * @param trueBranch    条件为真，跳转到的目标基本块
     * @param falseBranch   条件为假，跳转到的目标基本块
     */
    public Br(BasicBlock parent, Value cond, BasicBlock trueBranch, BasicBlock falseBranch) {
        // cond, trueBranch, falseBranch都是operands
        super("", new VoidType(), parent, cond, trueBranch, falseBranch);
        isConditional = true;
    }

    public String toString() {
        if (isConditional) {
            // 有条件跳转
            return "br " +
                    IrUtils.typeAndNameStr(getOperands().get(0)) + ", " +
                    IrUtils.typeAndNameStr(getOperands().get(1)) + ", " +
                    IrUtils.typeAndNameStr(getOperands().get(2));
        } else {
            // 无条件跳转
            return "br " + IrUtils.typeAndNameStr(getOperands().get(0));
        }
    }

    public void buildMips() {
        // 0. 把跳转块转换成mips
        MipsBlock curBlock = MipsContext.getBasicBlock(getParent());
        // 1. 有条件跳转: br i1 <cond>, label <iftrue>, label <iffalse>
        if (isConditional) {
            // 1.1 得到MIPS跳转块
            MipsBlock trueBlock = MipsContext.getBasicBlock((BasicBlock) getOp(2));
            MipsBlock falseBlock = MipsContext.getBasicBlock((BasicBlock) getOp(3));
            // 1.2 得到MIPS跳转条件类
            Icmp cond = (Icmp) getOp(1);
            MipsCondType type = MipsCondType.ir2MipsCondType(cond.getCondType());
            // 1.3 提取出icmp指令中的两个比较数：icmp slt i32 %i4, %i5
            MipsOperand op1 = MipsBuilder.buildOperand(cond.getOp(1), false, MipsContext.curIrFunction, getParent());
            MipsOperand op2 = MipsBuilder.buildOperand(cond.getOp(2), true, MipsContext.curIrFunction, getParent());
            // 1.4 把trueBranch设为跳转地址：blt v0, v2, b1_1；登记后继块
            MipsBuilder.buildBranch(type, op1, op2, trueBlock, curBlock);
            curBlock.setTrueSuccessor(trueBlock);
            curBlock.setFalseSuccessor(falseBlock);
        }
        // 2. 无条件跳转: br label <dest>
        else {
            // 2.1 得到MIPS跳转块
            MipsBlock destBlock = MipsContext.getBasicBlock((BasicBlock) getOp(1));
            // 2.2 构建跳转指令：j b1_1
            MipsBuilder.buildBranch(destBlock, curBlock);
            curBlock.setTrueSuccessor(destBlock);
        }
    }
}
