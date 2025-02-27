package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.values.Value;
import ir.values.instructions.Icmp;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 关系表达式
 * @date 2024/10/13 10:56
 * RelExp -> AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 */
public class RelExpNode {
    private AddExpNode addExpNode;
    private Token op;
    private RelExpNode relExpNode;

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }

    public RelExpNode(AddExpNode addExpNode, Token op, RelExpNode relExpNode) {
        this.addExpNode = addExpNode;
        this.op = op;
        this.relExpNode = relExpNode;
    }

//    public void print() {
//        addExpNode.print();
//        IOUtils.write("<RelExp>\n");
//        if (op != null) {
//            IOUtils.write(op.toString());
//            relExpNode.print();
//        }
//    }

    public void print() {
        if (op == null) {
            addExpNode.print();
        } else {
            relExpNode.print();
            IOUtils.write(op.toString());
            addExpNode.print();
        }
        IOUtils.write("<RelExp>\n");
    }

    // RelExp -> AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // synValue是本层比较或者下层的结果，可能为i1，也可能为i32
    public void buildIr() {
        if (op == null) {
            addExpNode.buildIr();
        } else {
            relExpNode.buildIr();
            Value opLeft = IrContext.synValue;
            addExpNode.buildIr();
            Value opRight = IrContext.synValue;
            // 如果是i1类型，要扩展到i32类型的
            if (opLeft.getType().isI1()) {
                opLeft = IrBuilder.buildZextInstruction(opLeft, IrContext.curBlock);
            }
            if (opRight.getType().isI1()) {
                opRight = IrBuilder.buildZextInstruction(opRight, IrContext.curBlock);
            }
            // 比较类型
            Icmp.CondType condType;
            condType = switch (op.getType()) {
                case LSS -> Icmp.CondType.LSS;  // <
                case LEQ -> Icmp.CondType.LEQ;  // <=
                case GRE -> Icmp.CondType.GRE;  // >
                default -> Icmp.CondType.GEQ;   // >=
            };
            // 返回值为该次比较的结果
            IrContext.synValue = IrBuilder.buildICmpInstruction(opLeft, opRight, condType, IrContext.curBlock);
        }
    }
}
