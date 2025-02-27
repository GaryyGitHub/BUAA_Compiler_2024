package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 加减表达式
 * @date 2024/10/13 10:55
 * AddExp -> MulExp | AddExp ('+' | '−') MulExp
 */
public class AddExpNode {
    private MulExpNode mulExpNode;
    private Token op;
    private AddExpNode addExpNode;

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public AddExpNode(MulExpNode mulExpNode, Token op, AddExpNode addExpNode) {
        this.mulExpNode = mulExpNode;
        this.op = op;
        this.addExpNode = addExpNode;
    }

//    public void print() {
//        mulExpNode.print();
//        IOUtils.write("<AddExp>\n");
//        if (op != null) {
//            IOUtils.write(op.toString());
//            addExpNode.print();
//        }
//    }
    public void print() {
        if (op == null) {
            mulExpNode.print();
        } else {
            addExpNode.print();
            IOUtils.write(op.toString());
            mulExpNode.print();
        }
        IOUtils.write("<AddExp>\n");
    }

    /**
     * 所有EXP指令，有三种工作类型
     * 1. 直接build下一层而无需新建指令
     * 2. build完下一层后，组装下层的synInt/synValue(取决于是否为常量)，向上传递synInt/synValue
     * 3. 自己就是synInt/synValue的源头，直接向上传递
     */
    // AddExp -> MulExp | AddExp ('+' | '−') MulExp
    public void buildIr() {
        // 1. 常量
        if (IrContext.isBuildingConstExp) {
            if(op == null) {
                mulExpNode.buildIr();
            } else {
                // addExp和mulExp的buildIr都会对synInt进行赋值，因此每步后的synInt都不一样
                addExpNode.buildIr();
                int ans = IrContext.synInt;
                mulExpNode.buildIr();
                if (op.getType() == TokenType.PLUS) {
                    ans += IrContext.synInt;
                } else {
                    ans -= IrContext.synInt;
                }
                IrContext.synInt = ans;
            }
        }
        // 2. 变量
        else {
            if(op == null) {
                mulExpNode.buildIr();
            } else {
                addExpNode.buildIr();
                Value opLeft = IrContext.synValue;
                mulExpNode.buildIr();
                Value opRight = IrContext.synValue;
                if (op.getType() == TokenType.PLUS) {
                    IrContext.synValue = IrBuilder.buildAddInstruction(opLeft, opRight, IrContext.curBlock);
                } else {
                    IrContext.synValue = IrBuilder.buildSubInstruction(opLeft, opRight, IrContext.curBlock);
                }
            }
        }
    }
}
