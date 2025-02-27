package nodes;

import utils.IOUtils;

/**
 * @author Gary
 * @Description: 表达式
 * @date 2024/10/13 10:52
 * Exp -> AddExp
 */
public class ExpNode {
    private AddExpNode addExpNode;

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public void print() {
        addExpNode.print();
        IOUtils.write("<Exp>\n");
    }

    public void buildIr() {
        addExpNode.buildIr();
    }
}
