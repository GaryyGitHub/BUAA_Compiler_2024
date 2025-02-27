package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.types.PointerType;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 基本表达式
 * @date 2024/10/13 10:53
 * PrimaryExp -> '(' Exp ')' | LVal | Number | Character
 */
public class PrimaryExpNode {
    // 这里就给出三种构造函数了，具体怎么预读，交给parser去做吧
    private Token leftParent = null;
    private ExpNode expNode = null;
    private Token rightParent = null;
    private LValNode lVal = null;
    private NumberNode numberNode = null;
    private CharacterNode characterNode = null;

    public ExpNode getExpNode() {
        return expNode;
    }

    public LValNode getLVal() {
        return lVal;
    }

    public NumberNode getNumberNode() {
        return numberNode;
    }

    public CharacterNode getCharacterNode() {
        return characterNode;
    }

    public PrimaryExpNode(Token leftParent, ExpNode expNode, Token rightParent) {
        this.leftParent = leftParent;
        this.expNode = expNode;
        this.rightParent = rightParent;
    }
    public PrimaryExpNode(LValNode lVal) {
        this.lVal = lVal;
    }
    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }
    public PrimaryExpNode(CharacterNode characterNode) {
        this.characterNode = characterNode;
    }
    
    public void print() {
        if (expNode != null) {
            IOUtils.write(leftParent.toString());
            expNode.print();
            IOUtils.write(rightParent.toString());
        } else if (lVal != null) {
            lVal.print();
        } else if (numberNode != null) {
            numberNode.print();
        } else if (characterNode != null) {
            characterNode.print();
        }
        IOUtils.write("<PrimaryExp>\n");
    }

    // PrimaryExp -> '(' Exp ')' | LVal | Number | Character
    public void buildIr() {
        // 1. 常量
        if (IrContext.isBuildingConstExp) {
            if (expNode != null) {
                expNode.buildIr();
            } else if (lVal != null) {
                lVal.buildIr();
            } else if (numberNode != null) {
                numberNode.buildIr();
            } else {
                characterNode.buildIr();
            }
        }
        // 2. 变量
        else {
            // FIXME: 有何用意？
            if (lVal != null) {
                // 如果正在加载函数参数，并且要求指针类型的value，就不load了
                // 需要消除isBuildingPointerRParam的标记，因为后续可能还进入primaryExp
                if (IrContext.isBuildingPointerRParam) {
                    IrContext.isBuildingPointerRParam = false;
                    lVal.buildIr();
                }
                // 如果是指针类型，那么进行加载。
                // 指针类型在通常状态下的加载，即在此实现（这是所有代码唯二两次load的地方）
                else {
                    lVal.buildIr();
                    if (IrContext.synValue.getType() instanceof PointerType) {
                        IrContext.synValue = IrBuilder.buildLoadInstruction(IrContext.synValue, IrContext.curBlock);
                    }
                }
            } else if (expNode != null) {
                expNode.buildIr();
            } else if (numberNode != null) {
                numberNode.buildIr();
            } else {
                characterNode.buildIr();
            }
        }
    }
}
