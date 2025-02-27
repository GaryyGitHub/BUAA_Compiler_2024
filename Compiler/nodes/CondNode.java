package nodes;

import ir.values.BasicBlock;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 条件表达式
 * @date 2024/10/13 10:52
 * Cond -> LOrExp
 */
public class CondNode {
    private LOrExpNode lOrExpNode;

    public LOrExpNode getLOrExpNode() {
        return lOrExpNode;
    }

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }

    public void print() {
        lOrExpNode.print();
        IOUtils.write("<Cond>\n");
    }

    // 以下都是ir相关用到的
    // cond == true时, 对应基本块分支
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;

    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }
    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    // CondNode用在涉及条件判断的语句（if和for），向下一层的LOrExpNode传递true和false分支
    public void buildIr() {
        lOrExpNode.setTrueBranch(trueBranch);
        lOrExpNode.setFalseBranch(falseBranch);
        lOrExpNode.buildIr();
    }
}
