package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Mul;
import ir.values.instructions.Sdiv;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 乘除模表达式
 * @date 2024/10/13 10:55
 * MulExp -> UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 */
public class MulExpNode {
    private UnaryExpNode unaryExpNode;
    private Token op;
    private MulExpNode mulExpNode;

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    public MulExpNode(UnaryExpNode unaryExpNode, Token op, MulExpNode mulExpNode) {
        this.unaryExpNode = unaryExpNode;
        this.op = op;
        this.mulExpNode = mulExpNode;
    }

    // 学长的：尾递归
    // 转化成右递归：MulExp -> UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
//    public void print() {
//        unaryExpNode.print();
//        IOUtils.write("<MulExp>\n");
//        if (op != null) {
//            IOUtils.write(op.toString());
//            mulExpNode.print();
//        }
//    }

    // 11.22 修改：语法分析改成了左递归，因此print也要改
    // MulExp -> UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public void print() {
        if (op == null) {
            unaryExpNode.print();
        } else {
            mulExpNode.print();
            IOUtils.write(op.toString());
            unaryExpNode.print();
        }
        IOUtils.write("<MulExp>\n");
    }

    // MulExp -> UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public void buildIr() {
        // 1. 常量，直接在现有的常数值上进行计算
        if (IrContext.isBuildingConstExp) {
            if (op == null) {
                unaryExpNode.buildIr();
            } else {
                // mulExp和unaryExp的buildIr都会对synInt进行赋值，因此每步后的synInt都不一样
                mulExpNode.buildIr();
                int ans = IrContext.synInt;
                unaryExpNode.buildIr();
                if (op.getType() == TokenType.MULT) {
                    ans *= IrContext.synInt;
                } else if (op.getType() == TokenType.DIV) {
                    ans /= IrContext.synInt;
                } else if (op.getType() == TokenType.MOD) {
                    ans %= IrContext.synInt;
                }
                // 最后的结果保存在synInt中，供更高层使用
                IrContext.synInt = ans;
            }
        }
        // 2. 变量，生成临时变量，并进行计算
        else {
            if (op == null) {
                unaryExpNode.buildIr();
            } else {
                mulExpNode.buildIr();
                Value opLeft = IrContext.synValue;
                unaryExpNode.buildIr();
                Value opRight = IrContext.synValue;
                if (op.getType() == TokenType.MULT) {
                    IrContext.synValue = IrBuilder.buildMulInstruction(opLeft, opRight, IrContext.curBlock);
                } else if (op.getType() == TokenType.DIV) {
                    IrContext.synValue = IrBuilder.buildSdivInstruction(opLeft, opRight, IrContext.curBlock);
                } else if (op.getType() == TokenType.MOD) {
                    // 取模可以替换为公式：x % y = x - ( x / y ) * y
                    // FIXME: 只有右操作数是常量且为1才srem，是为了后端优化：不需要div+mfhi指令，只需要move $zero即可
                    if (opRight instanceof ConstInt && ((ConstInt) opRight).getValue() == 1) {
                        IrContext.synValue = IrBuilder.buildSremInstruction(opLeft, opRight, IrContext.curBlock);
                    } else {
                        // div = x / y
                        Sdiv div = IrBuilder.buildSdivInstruction(opLeft, opRight, IrContext.curBlock);
                        // mul = div * y = ( x / y ) * y
                        Mul mul = IrBuilder.buildMulInstruction(div, opRight, IrContext.curBlock);
                        // 最后结果 = x - mul
                        IrContext.synValue = IrBuilder.buildSubInstruction(opLeft, mul, IrContext.curBlock);
                    }
                }
            }
        }
    }
}
