package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.values.BasicBlock;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 逻辑或表达式
 * @date 2024/10/13 10:57
 * LOrExp -> LAndExp | LOrExp '||' LAndExp
 */
public class LOrExpNode {
    private LAndExpNode lAndExpNode;
    private Token op;
    private LOrExpNode lOrExpNode;

    public LAndExpNode getLAndExpNode() {
        return lAndExpNode;
    }

    public LOrExpNode getLOrExpNode() {
        return lOrExpNode;
    }

    public LOrExpNode(LAndExpNode lAndExpNode, Token op, LOrExpNode lOrExpNode) {
        this.lAndExpNode = lAndExpNode;
        this.op = op;
        this.lOrExpNode = lOrExpNode;
    }

//    public void print() {
//        lAndExpNode.print();
//        IOUtils.write("<LOrExp>\n");
//        if (op != null) {
//            IOUtils.write(op.toString());
//            lOrExpNode.print();
//        }
//    }

    public void print() {
        if (op == null) {
            lAndExpNode.print();
        } else {
            lOrExpNode.print();
            IOUtils.write(op.toString());
            lAndExpNode.print();
        }
        IOUtils.write("<LOrExp>\n");
    }

    // ir中用到：设置分支
    // 满足条件为真或为假时跳转到的分支
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;
    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }
    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    // LOrExp -> LAndExp | LOrExp '||' LAndExp
    /**
     * LAndExp和LOrExp都需要考虑短路求值。
     * 在短路求值中，所有条件判断都被拆分，并被转化为基本块间的跳转关系。
     * LOrExp的作用是为下层的LAnd构建跳转块。
     *
     * 对于LOrExp '||' LAndExp：
     * 若LOrExp为真，则直接跳转到trueBranch
     * 若LOrExp为假，则进行短路求值，创建并跳转到一个新的块，在新块中处理LAndExp，避免了求LOrExp
     */
    public void buildIr() {
        // LOrExp -> LAndExp
        if (op == null) {
            handleOnlyLAndExp();
        }
        // LOrExp -> LOrExp '||' LAndExp
        else {
            BasicBlock scBranch = IrBuilder.buildBasicBlock(IrContext.curFunction);
            lOrExpNode.setTrueBranch(trueBranch);
            lOrExpNode.setFalseBranch(scBranch);
            lOrExpNode.buildIr();

            // 切换到新建的scBranch，在其中构建LAndExp
            IrContext.curBlock = scBranch;
            handleOnlyLAndExp();
        }
    }

    // 处理只有LandExp的情形：LOrExp -> LAndExp
    // 只需按情况处理trueBranch/falseBranch即可
    private void handleOnlyLAndExp() {
        lAndExpNode.setTrueBranch(trueBranch);
        lAndExpNode.setFalseBranch(falseBranch);
        lAndExpNode.buildIr();
    }
}
