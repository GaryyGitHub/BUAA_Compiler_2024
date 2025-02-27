package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.IntType;
import ir.values.constants.ConstInt;
import utils.IOUtils;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 主函数定义
 * @date 2024/10/13 10:50
 * MainFuncDef -> 'int' 'main' '(' ')' Block
 */
public class MainFuncDefNode {
    private Token intToken, mainToken, lParenToken, rParenToken;
    private BlockNode blockNode;

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public MainFuncDefNode(Token intToken, Token mainToken, Token lParenToken, Token rParenToken, BlockNode blockNode) {
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lParenToken = lParenToken;
        this.rParenToken = rParenToken;
        this.blockNode = blockNode;
    }

    public void print() {
        IOUtils.write(intToken.toString());
        IOUtils.write(mainToken.toString());
        IOUtils.write(lParenToken.toString());
        IOUtils.write(rParenToken.toString());
        blockNode.print();
        IOUtils.write("<MainFuncDef>\n");
    }

    // MainFuncDef -> 'int' 'main' '(' ')' Block
    // 开头一套连招和FuncDefNode一样，只是不用考虑函数返回值类型了
    public void buildIr() {
        IrContext.intBits = 32;
        // 1. 创建函数定义
        IrContext.curFunction = IrBuilder.buildFunction(mainToken.getValue(), new IntType(32), new ArrayList<>(), false);
        // 2. 新建一个符号表并入栈，作为函数符号表
        IrContext.curFunction.setSymbolTable(IrSymTableStack.push());
        // 3. 构建下属的第一个基本块
        IrContext.curBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);
        // 解析函数体
        blockNode.buildIr();

        // 若最后不是return语句（但是好像不会发生？），添加return 0
        if(!BlockNode.isReturnEnd(blockNode)) {
            IrBuilder.buildRetInstruction(IrContext.curBlock, ConstInt.ZERO(32));
        }

        // 符号表出栈！
        IrSymTableStack.pop();
    }
}
