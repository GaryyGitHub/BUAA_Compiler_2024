package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.constants.ConstInt;
import utils.IOUtils;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 函数定义
 * @date 2024/10/13 10:48
 * FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
 */
public class FuncDefNode {
    private FuncTypeNode funcTypeNode;
    private Token ident;
    private Token leftParent;
    private FuncFParamsNode funcFParamsNode;
    private Token rightParent;
    private BlockNode blockNode;

    public FuncTypeNode getFuncTypeNode() {
        return funcTypeNode;
    }

    public FuncFParamsNode getFuncFParamsNode() {
        return funcFParamsNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public Token getIdent() {
        return ident;
    }

    public FuncDefNode(FuncTypeNode funcTypeNode, Token ident, Token leftParent, FuncFParamsNode funcFParamsNode, Token rightParent, BlockNode blockNode) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
        this.leftParent = leftParent;
        this.funcFParamsNode = funcFParamsNode;
        this.rightParent = rightParent;
        this.blockNode = blockNode;
    }

    public void print() {
        funcTypeNode.print();
        IOUtils.write(ident.toString());
        IOUtils.write(leftParent.toString());
        if (funcFParamsNode != null) {
            funcFParamsNode.print();
        }
        IOUtils.write(rightParent.toString());
        blockNode.print();
        IOUtils.write("<FuncDef>\n");
    }

    /**
     * 构建函数定义，对函数参数进行alloca，但函数参数的解析在FuncFParamsNode（形参）中进行
     * FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
     */
    public void buildIr() {
        // 0. 先处理类型信息, Gary自创的，需要接收BTypeNode作为参数以判断是int还是char
        TokenType funcTypeNodeType = funcTypeNode.getType();
        // TODO: 虽然funcType有void，但void那里应该调用不到intBits，要检查一下
        IrContext.intBits = (funcTypeNodeType == TokenType.INTTK) ? 32 : 8;
        // 1. 函数返回值类型
        ValueType returnType = funcTypeNode.getIrReturnType();
        // 2. 创建函数定义
        IrContext.curFunction = IrBuilder.buildFunction(ident.getValue(), returnType, new ArrayList<>(), false);
        // 3. 新建一个符号表并入栈，作为函数符号表
        IrContext.curFunction.setSymbolTable(IrSymTableStack.push());
        // 4. 构建下属的第一个基本块
        IrContext.curBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);

        // 若函数有参数，构建函数参数Ir
        if (funcFParamsNode != null) {
            funcFParamsNode.buildIr();
        }
        // 解析函数体
        blockNode.buildIr();
        // 若函数体最后没有return，需要添加一个return语句
        if (!BlockNode.isReturnEnd(blockNode)) {
            if(returnType instanceof VoidType) {
                // 如果这是个void函数，则需要添加ret void
                IrBuilder.buildRetInstruction(IrContext.curBlock, null);
            } else {
                // 否则，需要添加ret 0
                IrBuilder.buildRetInstruction(IrContext.curBlock, ConstInt.ZERO(IrContext.intBits));
            }
        }
        // 符号表出栈！
        IrSymTableStack.pop();
    }
}
