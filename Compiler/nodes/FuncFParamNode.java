package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrContext;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 函数形参
 * @date 2024/10/13 10:50
 * FuncFParam -> BType Ident ['[' ']']
 */
public class FuncFParamNode {
    private BTypeNode bTypeNode;
    private Token ident;
    private Token leftBracket;
    private Token rightBracket;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, Token leftBracket, Token rightBracket) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
    }

    public BTypeNode getBTypeNode() {
        return bTypeNode;
    }

    public Token getIdent() {
        return ident;
    }

    public Token getLeftBracket() {
        return leftBracket;
    }

    public void print() {
        bTypeNode.print();
        IOUtils.write(ident.toString());
        if (leftBracket != null) {
            IOUtils.write(leftBracket.toString());
            IOUtils.write(rightBracket.toString());
        }
        IOUtils.write("<FuncFParam>\n");
    }

    // FuncFParam -> BType Ident ['[' ']']
    // 解析参数，并以ValueType的形式传入function，以记录参数
    public void buildIr() {
        // 0. 先处理类型信息, Gary自创的，需要接收BTypeNode作为参数以判断是int还是char
        TokenType bTypeNodeType = bTypeNode.getType();
        IrContext.intBits = (bTypeNodeType == TokenType.INTTK) ? 32 : 8;
        // 1. 定义参数类型
        ValueType paramType = new IntType(IrContext.intBits);
        // 2. 分类讨论
        if (leftBracket != null) {
            // 传参是数组，则得到指针。
            // 需注意，得到的并非“指向数组整体”的指针，而是“指向数组下一级元素”的指针，这是为了store方便考虑
            paramType = new PointerType(paramType);
        }
        // 3. 把解析完成的参数类型传给curFunction, 在curFunction内部构建参数value
        IrContext.curFunction.addArgsByValueType(paramType, IrContext.inheritInt);
    }
}
