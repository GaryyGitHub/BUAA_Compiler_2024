package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.Function;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Icmp;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 一元表达式
 * @date 2024/10/13 10:54
 * UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 */
public class UnaryExpNode {
    // 这里就给出两种构造函数了，具体怎么预读，交给parser去做吧
    private PrimaryExpNode primaryExpNode = null;
    private Token ident = null;
    private Token leftParent = null;
    private FuncRParamsNode funcRParamsNode = null;
    private Token rightParent = null;
    private UnaryOpNode unaryOpNode = null;
    private UnaryExpNode unaryExpNode = null;

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    public FuncRParamsNode getFuncRParamsNode() {
        return funcRParamsNode;
    }

    public Token getIdent() {
        return ident;
    }

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
    }

    public UnaryExpNode(Token ident, Token leftParent, FuncRParamsNode funcRParamsNode, Token rightParent) {
        this.ident = ident;
        this.leftParent = leftParent;
        this.funcRParamsNode = funcRParamsNode;
        this.rightParent = rightParent;
    }

    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }

    public void print() {
        if (primaryExpNode != null) {
            primaryExpNode.print();
        } else if (ident != null) {
            IOUtils.write(ident.toString());
            IOUtils.write(leftParent.toString());
            if (funcRParamsNode != null) {
                funcRParamsNode.print();
            }
            IOUtils.write(rightParent.toString());
        } else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        IOUtils.write("<UnaryExp>\n");
    }

    // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public void buildIr() {
        // 1. 常量，只可能是PrimaryExp | UnaryOp UnaryExp
        if (IrContext.isBuildingConstExp) {
            // 1.1 PrimaryExp
            if (primaryExpNode != null) {
                primaryExpNode.buildIr();
            }
            // 1.2 UnaryOp UnaryExp
            // UnaryOp -> '+' | '−' | '!'，在这里处理相关问题了，不去UnaryOp了
            else if (unaryExpNode != null) {
                // 此处就不用构建指令了，直接改值即可！
                unaryExpNode.buildIr();
                if (unaryOpNode.getOp().getType() == TokenType.MINU) {
                    IrContext.synInt = -IrContext.synInt;
                } else if (unaryOpNode.getOp().getType() == TokenType.NOT) {
                    IrContext.synInt = IrContext.synInt == 0 ? 1 : 0;
                }   // PLUS不处理
            }
        }
        // 2. 变量，三者均有可能
        else {
            if (primaryExpNode != null) {
                // 1. PrimaryExp
                primaryExpNode.buildIr();
            } else if (unaryExpNode != null) {
                // 2. UnaryOp UnaryExp
                unaryExpNode.buildIr();
                // 此处就必须构建指令了，因为变量经过一元表达式后的取值是由指令决定的！
                // FIXME: 这里有问题！所有涉及32的地方都应具体判断是int还是char！11.21解决，使用IrContext.intBits
                if (unaryOpNode.getOp().getType() == TokenType.MINU) {
                    IrContext.synValue = IrBuilder.buildSubInstruction(ConstInt.ZERO(32), IrContext.synValue, IrContext.curBlock);
                } else if (unaryOpNode.getOp().getType() == TokenType.NOT) {
                    // icmp指令得到的是i1类型的值，需要转换成i32
                    IrContext.synValue = IrBuilder.buildICmpInstruction(
                        ConstInt.ZERO(32), IrContext.synValue, Icmp.CondType.EQL, IrContext.curBlock
                    );
                    IrContext.synValue = IrBuilder.buildZextInstruction(IrContext.synValue, IrContext.curBlock);
                }
            } else if (ident != null) {
                // 3. Ident '(' [FuncRParams] ')'
                Function function = (Function) IrSymTableStack.getSymbol(ident.getValue());
                // 实参列表
                ArrayList<Value> argRValues = new ArrayList<>();
                // 3.1 如果带有实参
                if (funcRParamsNode != null) {
                    // 形参列表
                    ArrayList<Value> argFValues = function.getArgValues();
                    // 实参的node形式
                    List<ExpNode> expNodes = funcRParamsNode.getExpNodes();
                    // 逐个参数地解析
                    for (int i = 0; i < expNodes.size(); i++) {
                        // 形参要求的类型
                        ValueType fType = argFValues.get(i).getType();
                        // 如果形参要求指针类型，那么后续遇到synValue第一次是指针类型时，不进行store操作
                        if (fType instanceof PointerType) {
                            IrContext.isBuildingPointerRParam = true;
                        }
                        // 在这里解析各个实参，FuncRParams就不干事儿了，没有buildIr()方法
                        expNodes.get(i).buildIr();
                        // 把解析好的实参放入argRValues
                        argRValues.add(IrContext.synValue);
                        IrContext.isBuildingPointerRParam = false;
                    }
                }   // 3.2 如果没有实参，不处理
                // 参数解析均完成，构建指令
                IrContext.synValue = IrBuilder.buildCallInstruction(function, argRValues, IrContext.curBlock);
            }
        }
    }
}
