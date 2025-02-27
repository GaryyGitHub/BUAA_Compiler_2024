package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.values.Value;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: For语句
 * @date 2024/10/13 10:52
 * ForStmt -> LVal '=' Exp
 */
public class ForStmtNode {
    private LValNode lValNode;
    private Token assign;
    private ExpNode expNode;

    public LValNode getLValNode() {
        return lValNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public ForStmtNode(LValNode lValNode, Token assign, ExpNode expNode) {
        this.lValNode = lValNode;
        this.assign = assign;
        this.expNode = expNode;
    }

    public void print() {
        lValNode.print();
        IOUtils.write(assign.toString());
        expNode.print();
        IOUtils.write("<ForStmt>\n");
    }

    // ForStmt -> LVal '=' Exp
    public void buildIr() {
        lValNode.buildIr();
        Value lVal = IrContext.synValue;
        expNode.buildIr();
        Value exp = IrContext.synValue;
        IrBuilder.buildStoreInstruction(exp, lVal, IrContext.curBlock);
    }
}
