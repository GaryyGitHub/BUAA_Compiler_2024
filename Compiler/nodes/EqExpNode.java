package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.values.Value;
import ir.values.instructions.Icmp;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 相等性表达式
 * @date 2024/10/13 10:56
 * EqExp -> RelExp | EqExp ('==' | '!=') RelExp
 */
public class EqExpNode {
    private RelExpNode relExpNode;
    private Token op;
    private EqExpNode eqExpNode;

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    public EqExpNode(RelExpNode relExpNode, Token op, EqExpNode eqExpNode) {
        this.relExpNode = relExpNode;
        this.op = op;
        this.eqExpNode = eqExpNode;
    }

//    public void print() {
//        relExpNode.print();
//        IOUtils.write("<EqExp>\n");
//        if (op != null) {
//            IOUtils.write(op.toString());
//            eqExpNode.print();
//        }
//    }

    public void print() {
        if (op == null) {
            relExpNode.print();
        } else {
            eqExpNode.print();
            IOUtils.write(op.toString());
            relExpNode.print();
        }
        IOUtils.write("<EqExp>\n");
    }

    // EqExp -> RelExp | EqExp ('==' | '!=') RelExp
    // synValue是本层比较或者下层的结果，可能为i1，也可能为i32
    public void buildIr() {
        if (op == null) {
            relExpNode.buildIr();
        } else {
            eqExpNode.buildIr();
            Value opLeft = IrContext.synValue;
            relExpNode.buildIr();
            Value opRight = IrContext.synValue;
            // 如果是i1类型，要扩展到i32类型的
            if (opLeft.getType().isI1()) {
                opLeft = IrBuilder.buildZextInstruction(opLeft, IrContext.curBlock);
            }
            if (opRight.getType().isI1()) {
                opRight = IrBuilder.buildZextInstruction(opRight, IrContext.curBlock);
            }
            // 比较类型
            Icmp.CondType condType = (op.getType() == TokenType.EQL) ? Icmp.CondType.EQL : Icmp.CondType.NEQ;
            // 返回值为该次比较的结果
            IrContext.synValue = IrBuilder.buildICmpInstruction(opLeft, opRight, condType, IrContext.curBlock);
        }
    }
}
