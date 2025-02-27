package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Icmp;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 逻辑与表达式
 * @date 2024/10/13 10:56
 * LAndExp -> EqExp | LAndExp '&&' EqExp
 */
public class LAndExpNode {
    private EqExpNode eqExpNode;
    private Token op;
    private LAndExpNode lAndExpNode;

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    public LAndExpNode getLAndExpNode() {
        return lAndExpNode;
    }

    public LAndExpNode(EqExpNode eqExpNode, Token op, LAndExpNode lAndExpNode) {
        this.eqExpNode = eqExpNode;
        this.op = op;
        this.lAndExpNode = lAndExpNode;
    }

//    public void print() {
//        eqExpNode.print();
//        IOUtils.write("<LAndExp>\n");
//        if (op != null) {
//            IOUtils.write(op.toString());
//            lAndExpNode.print();
//        }
//    }

    public void print() {
        if (op == null) {
            eqExpNode.print();
        } else {
            lAndExpNode.print();
            IOUtils.write(op.toString());
            eqExpNode.print();
        }
        IOUtils.write("<LAndExp>\n");
    }

    // LAndExp -> EqExp | LAndExp '&&' EqExp
    /**
     * LAndExp和LOrExp都需要考虑短路求值。
     * 在短路求值中，所有条件判断都被拆分，并被转化为基本块间的跳转关系。
     * LOrExp的作用是为下层的LAnd构建跳转块。
     *
     * 对于EqExp：
     * 已经到达求值的最高级本单元，需要根据这个EqExp构造br指令
     *
     * 对于LAndExp '&&' EqExp：
     * 若LAndExp为假，则直接跳转到falseBranch
     * 若LAndExp为真，则进行短路求值，创建并跳转到一个新的块，在新块中处理EqExp，避免了求LAndExp
     */
    public BasicBlock trueBranch;
    public BasicBlock falseBranch;
    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }
    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    public void buildIr() {
        // LAndExp -> EqExp
        if (op == null) {
            handleOnlyEqExp();
        }
        // LAndExp -> LAndExp '&&' EqExp
        else {
            // 短路求值：short-circuit evaluation
            // 这个scBranch就代替trueBranch，在里面处理EqExp。在LandExp为真时，体现为不处理后面的EqExp
            BasicBlock scBranch = IrBuilder.buildBasicBlock(IrContext.curFunction);
            lAndExpNode.setTrueBranch(scBranch);
            lAndExpNode.setFalseBranch(falseBranch);
            lAndExpNode.buildIr();

            // 切换到新建的scBranch，在其中构建EqExp
            IrContext.curBlock = scBranch;
            handleOnlyEqExp();
        }
    }

    // 处理只有EqExp的情形: LAndExp -> EqExp
    private void handleOnlyEqExp() {
        eqExpNode.buildIr();
        // 此处是有条件跳转语句，并且br要求的condition类型为i1，但synValue可能为i32/i8
        Value condition = IrContext.synValue;
        IntType valueType = (IntType) condition.getType();
        if (!valueType.isI1()) {
            // 如果synValue的类型不是i1，这里巧妙：则通过icmp转换一下，用trunc也不好
            // 判断：若i32/i8的值非0(也是i32/i8类型的)，则为true，否则为false
            condition = IrBuilder.buildICmpInstruction(condition, ConstInt.ZERO(valueType.getBits()), Icmp.CondType.NEQ, IrContext.curBlock);
        }
        // 进行有条件跳转
        IrBuilder.buildBrInstruction(condition, trueBranch, falseBranch, IrContext.curBlock);
        // 一切ConstInt在这里都转化成了icmp
        // icmp会返回IntType(i1)类型的指令，因此带条件br语句的condition永远是icmp类型
    }
}
