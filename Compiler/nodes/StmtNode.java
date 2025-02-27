package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;
import utils.IOUtils;
import utils.IrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 语句
 * @date 2024/10/13 10:51
 * Stmt -> LVal '=' Exp ';'
        | [Exp] ';'
        | Block
        | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt`
        | 'break' ';' | 'continue' ';'
        | 'return' [Exp] ';'
        | LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
        | 'printf''('StringConst {','Exp}')'';'
 */
public class StmtNode {
    // 枚举stmt类型
    public enum StmtType {
        ASSIGN, EXP, BLOCK, IF, FOR, BREAK, CONTINUE, RETURN, GETINT, GETCHAR, PRINTF
    }
    private StmtType type;
    private LValNode lValNode;
    private Token assignToken;
    private ExpNode expNode;
    private Token semicolonToken;
    private BlockNode blockNode;
    private Token ifToken;
    private Token leftParenToken;
    private CondNode condNode;
    private Token rightParenToken;
    private List<StmtNode> stmtNodes;
    private Token elseToken;
    private Token forToken;
    private ForStmtNode forStmtNode1;
    private Token semicolonToken2;
    private ForStmtNode forStmtNode2;
    private Token breakContinueToken;
    private Token returnToken;
    private Token getintGetCharToken;
    private Token printfToken;
    private Token StringConstToken;
    private List<Token> commas;
    private List<ExpNode> expNodes;

    // 语义分析用到，处理g类错误
    public Token getReturnToken() {
        return returnToken;
    }

    public LValNode getLValNode() {
        return lValNode;
    }

    // 语义分析分类处理
    public StmtType getType() {
        return type;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public List<StmtNode> getStmtNodes() {
        return stmtNodes;
    }

    public Token getElseToken() {
        return elseToken;
    }

    public ForStmtNode getForStmtNode1() {
        return forStmtNode1;
    }

    public ForStmtNode getForStmtNode2() {
        return forStmtNode2;
    }

    public Token getBreakContinueToken() {
        return breakContinueToken;
    }

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }

    public Token getPrintfToken() {
        return printfToken;
    }

    public Token getStringConstToken() {
        return StringConstToken;
    }

    // 每种stmt分别对应一种构造函数，break和continue合二为一，getint和getchar合二为一
    public StmtNode(StmtType type, LValNode lValNode, Token assignToken, ExpNode expNode, Token semicolonToken) {
        // LVal '=' Exp ';'
        this.type = type;
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.expNode = expNode;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType type, ExpNode expNode, Token semicolonToken) {
        // [Exp] ';'
        this.type = type;
        this.expNode = expNode;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType type, BlockNode blockNode) {
        // Block
        this.type = type;
        this.blockNode = blockNode;
    }

    public StmtNode(StmtType type, Token ifToken, Token leftParenToken, CondNode condNode, Token rightParenToken, List<StmtNode> stmtNodes, Token elseToken) {
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        this.type = type;
        this.ifToken = ifToken;
        this.leftParenToken = leftParenToken;
        this.condNode = condNode;
        this.rightParenToken = rightParenToken;
        this.stmtNodes = stmtNodes;
        this.elseToken = elseToken;
    }

    public StmtNode(StmtType type, Token forToken, Token leftParenToken, ForStmtNode forStmtNode1, Token semicolonToken,
                    CondNode condNode, Token semicolonToken2, ForStmtNode forStmtNode2, Token rightParenToken, List<StmtNode> stmtNodes) {
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        this.type = type;
        this.forToken = forToken;
        this.leftParenToken = leftParenToken;
        this.forStmtNode1 = forStmtNode1;
        this.semicolonToken = semicolonToken;
        this.condNode = condNode;
        this.semicolonToken2 = semicolonToken2;
        this.forStmtNode2 = forStmtNode2;
        this.rightParenToken = rightParenToken;
        this.stmtNodes = stmtNodes;
    }

    public StmtNode(StmtType type, Token breakContinueToken, Token semicolonToken) {
        // 'break' ';' | 'continue' ';'
        this.type = type;
        this.breakContinueToken = breakContinueToken;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType type, Token returnToken, ExpNode expNode, Token semicolonToken) {
        // 'return' [Exp] ';'
        this.type = type;
        this.returnToken = returnToken;
        this.expNode = expNode;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType type, LValNode lValNode, Token assign, Token getintGetCharToken, Token leftParenToken, Token rightParenToken, Token semicolonToken) {
        // LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
        this.type = type;
        this.lValNode = lValNode;
        this.assignToken = assign;
        this.getintGetCharToken = getintGetCharToken;
        this.leftParenToken = leftParenToken;
        this.rightParenToken = rightParenToken;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType type, Token printfToken, Token leftParenToken, Token StringConstToken, List<Token> commas, List<ExpNode> expNodes, Token rightParenToken, Token semicolonToken) {
        // 'printf''('StringConst {','Exp}')'';'
        this.type = type;
        this.printfToken = printfToken;
        this.leftParenToken = leftParenToken;
        this.StringConstToken = StringConstToken;
        this.commas = commas;
        this.expNodes = expNodes;
        this.rightParenToken = rightParenToken;
        this.semicolonToken = semicolonToken;
    }

    public void print() {
        switch (type) {
            case ASSIGN:
                // LVal '=' Exp ';'
                lValNode.print();
                IOUtils.write(assignToken.toString());
                expNode.print();
                IOUtils.write(semicolonToken.toString());
                break;
            case EXP:
                // [Exp] ';'
                if (expNode != null)
                    expNode.print();
                IOUtils.write(semicolonToken.toString());
                break;
            case BLOCK:
                // Block
                blockNode.print();
                break;
            case IF:
                // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                IOUtils.write(ifToken.toString());
                IOUtils.write(leftParenToken.toString());
                condNode.print();
                IOUtils.write(rightParenToken.toString());
                stmtNodes.get(0).print();
                if (elseToken != null) {
                    IOUtils.write(elseToken.toString());
                    stmtNodes.get(1).print();
                }
                break;
            case FOR:
                // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                IOUtils.write(forToken.toString());
                IOUtils.write(leftParenToken.toString());
                if (forStmtNode1 != null)
                    forStmtNode1.print();
                IOUtils.write(semicolonToken.toString());
                if (condNode != null)
                    condNode.print();
                IOUtils.write(semicolonToken2.toString());
                if (forStmtNode2 != null) {
                    forStmtNode2.print();
                }
                IOUtils.write(rightParenToken.toString());
                stmtNodes.get(0).print();
                break;
            case BREAK:
            case CONTINUE:
                // 'break' ';' | 'continue' ';'
                IOUtils.write(breakContinueToken.toString());
                IOUtils.write(semicolonToken.toString());
                break;
            case RETURN:
                // 'return' [Exp] ';'
                IOUtils.write(returnToken.toString());
                if (expNode != null)
                    expNode.print();
                IOUtils.write(semicolonToken.toString());
                break;
            case GETINT:
            case GETCHAR:
                // LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
                lValNode.print();
                IOUtils.write(assignToken.toString());
                IOUtils.write(getintGetCharToken.toString());
                IOUtils.write(leftParenToken.toString());
                IOUtils.write(rightParenToken.toString());
                IOUtils.write(semicolonToken.toString());
                break;
            case PRINTF:
                // 'printf''('StringConst {','Exp}')'';'
                IOUtils.write(printfToken.toString());
                IOUtils.write(leftParenToken.toString());
                IOUtils.write(StringConstToken.toString());
                for (int i = 0; i < commas.size(); i++) {
                    IOUtils.write(commas.get(i).toString());
                    expNodes.get(i).print();
                }
                IOUtils.write(rightParenToken.toString());
                IOUtils.write(semicolonToken.toString());
                break;
        }
        IOUtils.write("<Stmt>\n");
    }

    /**
     * Stmt -> LVal '=' Exp ';'
     | [Exp] ';'
     | Block
     | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt`
     | 'break' ';' | 'continue' ';'
     | 'return' [Exp] ';'
     | LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';'
     | 'printf''('StringConst {','Exp}')'';'
     */
    public void buildIr() {
        switch (type) {
            // LVal '=' Exp ';'
            case ASSIGN -> buildAssignIr();
            // [Exp] ';'
            case EXP -> buildExpIr();
            // Block
            case BLOCK -> buildBlockIr();
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            case IF -> buildIfIr();
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            case FOR -> buildForIr();
            // 'break' ';'
            case BREAK -> buildBreakIr();
            // 'continue' ';'
            case CONTINUE -> buildContinueIr();
            // 'return' [Exp] ';'
            case RETURN -> buildReturnIr();
            // LVal '=' 'getint''('')'';'
            case GETINT -> buildGetintIr();
            // LVal '=' 'getchar''('')'';'
            case GETCHAR -> buildGetcharIr();
            // 'printf''('StringConst {','Exp}')'';'
            case PRINTF -> buildPrintfIr();
        }
    }

    // LVal '=' Exp ';'
    private void buildAssignIr() {
        lValNode.buildIr();
        Value lVal = IrContext.synValue;
        expNode.buildIr();
        Value exp = IrContext.synValue;
        // 向lVal所处地址存储exp的内容
        IrBuilder.buildStoreInstruction(exp, lVal, IrContext.curBlock);
    }

    // [Exp] ';'
    private void buildExpIr() {
        if (expNode != null) {
            expNode.buildIr();
        }
    }

    // Block
    private void buildBlockIr() {
        // 在此处创建符号表，调用结束pop销毁
        IrSymTableStack.push();
        blockNode.buildIr();
        IrSymTableStack.pop();
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    private void buildIfIr() {
        // ====== 1. 为解析Cond准备基本块 ======
        // trueBranch: cond为true时进入的块
        BasicBlock trueBranch = IrBuilder.buildBasicBlock(IrContext.curFunction);
        // nextBlock: if结束后跳转到的新块
        BasicBlock nextBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);
        // falseBranch: cond为false时进入的块，若没有else就是nextBlock
        BasicBlock falseBranch = nextBlock;
        // 如果有else，那么nextBranch就要新建啦
        if (elseToken != null) {
            falseBranch = IrBuilder.buildBasicBlock(IrContext.curFunction);
        }

        // ====== 2. 丝滑连招，解析Cond ======
        condNode.setTrueBranch(trueBranch);
        condNode.setFalseBranch(falseBranch);
        condNode.buildIr();

        // ====== 3. 在trueBranch中解析Stmt1，顺带跳转到nextBlock ======
        IrContext.curBlock = trueBranch;
        stmtNodes.get(0).buildIr();
        // 跳转语句，到if外面去
        IrBuilder.buildBrInstruction(nextBlock, IrContext.curBlock);

        // ====== 4. 若有else，则在falseBranch中解析Stmt2，顺带跳转到nextBlock ======
        if (elseToken != null) {
            IrContext.curBlock = falseBranch;
            stmtNodes.get(1).buildIr();
            IrBuilder.buildBrInstruction(nextBlock, IrContext.curBlock);
        }

        // ====== 5. 回到nextBlock，完成解析 ======
        IrContext.curBlock = nextBlock;
    }

    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    // FIXME: 11.19 具体逻辑还是没搞懂！
    private void buildForIr(){
        // ====== 0. 预备四个块：cond, loop, loopEnd, end ======
        // cond: 条件判断
        BasicBlock condBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);
        // loop: 循环体
        BasicBlock loopBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);
        // loopEnd: 自增块，需要跳转到cond
        BasicBlock loopEndBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);
        // end: 整个for结束，跳转到下一个基本块
        BasicBlock endBlock = IrBuilder.buildBasicBlock(IrContext.curFunction);

        // ====== 1. headBlock 解析forStmt1 ======
        if (forStmtNode1 != null) {
            forStmtNode1.buildIr();
        }
        // 跳转到condBlock
        IrBuilder.buildBrInstruction(condBlock, IrContext.curBlock);

        // ====== 2. condBlock 解析cond ======
        IrContext.curBlock = condBlock;
        if (condNode != null) {
            // 继续循环进入loopBlock，否则结束整个for进入endBlock
            condNode.setTrueBranch(loopBlock);
            condNode.setFalseBranch(endBlock);
            condNode.buildIr();
        } else {
            // 没有条件，直接进入loopBlock
            IrBuilder.buildBrInstruction(loopBlock, IrContext.curBlock);
        }

        // ====== 3. loopEndBlock 解析forStmt2 但是它会放在loopBlock的后面 ======
        IrContext.curBlock = loopEndBlock;
        if (forStmtNode2 != null) {
            forStmtNode2.buildIr();
        }
        // 跳转到condBlock
        IrBuilder.buildBrInstruction(condBlock, IrContext.curBlock);

        // ====== 4. loopBlock 解析stmt ======
        IrContext.curBlock = loopBlock;
        // 额外设置好loopEndBlock栈和endBlock栈，分别用于continue和break跳转
        IrContext.loopEndBlockStack.push(loopEndBlock);
        // break是进入外层的基本块，continue是从
        IrContext.endBlockStack.push(endBlock);
        // 解析stmt
        stmtNodes.get(0).buildIr();
        // 完成当前解析，弹栈
        IrContext.loopEndBlockStack.pop();
        IrContext.endBlockStack.pop();
        // loopBlock结束后跳转到自增块loopEndBlock
        IrBuilder.buildBrInstruction(loopEndBlock, IrContext.curBlock);

        // ====== 5. 解析完毕 ======
        IrContext.curBlock = endBlock;
    }

    // 'break' ';'
    private void buildBreakIr() {
        // 强制跳转到endBlock
        IrBuilder.buildBrInstruction(IrContext.endBlockStack.peek(), IrContext.curBlock);
        // break后的代码失效，因此应该新建一个块，将其附着在新的Function上面，但function不加入module，实现丢弃代码的效果
        IrContext.curBlock = new BasicBlock("dead_block_break", new Function("dead_function_break", new VoidType(), new ArrayList<>(), false));
    }

    // 'continue' ';'
    private void buildContinueIr() {
        // 强制跳转到loopEndBlock
        IrBuilder.buildBrInstruction(IrContext.loopEndBlockStack.peek(), IrContext.curBlock);
        // continue后的代码失效，因此应该新建一个块，将其附着在新的Function上面，但function不加入module，实现丢弃代码的效果
        IrContext.curBlock = new BasicBlock("dead_block_continue", new Function("dead_function_continue", new VoidType(), new ArrayList<>(), false));
    }

    // 'return' [Exp] ';'
    private void buildReturnIr() {
        Value retExp = null;
        if(expNode != null) {
            expNode.buildIr();
            // 执行完buildIr，把buildIr存入synValue的值取出来
            retExp = IrContext.synValue;
        }
        IrBuilder.buildRetInstruction(IrContext.curBlock, retExp);
    }

    // LVal '=' 'getint''('')'';'
    private void buildGetintIr() {
        lValNode.buildIr();
        Value lVal = IrContext.synValue;
        // 调用getint
        // 没有参数，实参列表就是空list
        Value getIntResult = IrBuilder.buildCallInstruction(Function.getint, new ArrayList<>(), IrContext.curBlock);
        // 把读到的值存入lVal
        IrBuilder.buildStoreInstruction(getIntResult, lVal, IrContext.curBlock);
    }

    // LVal '=' 'getchar''('')'';'
    private void buildGetcharIr() {
        lValNode.buildIr();
        Value lVal = IrContext.synValue;
        // 调用getchar
        // 没有参数，实参列表就是空list
        Value getCharResult = IrBuilder.buildCallInstruction(Function.getchar, new ArrayList<>(), IrContext.curBlock);
        // 把读到的值存入lVal
        IrBuilder.buildStoreInstruction(getCharResult, lVal, IrContext.curBlock);
    }

    // 'printf''('StringConst {','Exp}')'';'
    private void buildPrintfIr() {
        ArrayList<Value> params = new ArrayList<>();
        // 解析Exp，将解析出来的格式化变量存入params中
        for (ExpNode expNode1: expNodes) {
            expNode1.buildIr();
            params.add(IrContext.synValue);
        }
        String formatStr = StringConstToken.getValue();
        // 把stringConst分割成多个字符串便于输出
        ArrayList<String> splitStrings = IrUtils.splitFormatString(formatStr);

        int expIndex = 0;
        for (String splitStr: splitStrings) {
            // %d或%c, 改为输出对应的exp
            if(splitStr.equals("%d")) {
                Value param = params.get(expIndex);
                IrBuilder.buildCallInstruction(Function.putint, new ArrayList<>() {{
                    add(param);
                }}, IrContext.curBlock);
                expIndex++;
            } else if(splitStr.equals("%c")) {
                Value param = params.get(expIndex);
                IrBuilder.buildCallInstruction(Function.putch, new ArrayList<>() {{
                    add(param);
                }}, IrContext.curBlock);
                expIndex++;
            } else {
                // 常量字符串，构造全局变量，输出
                // 全局变量[n x i8]*
                Value strGlobalVar = IrBuilder.buildGlobalConstString(splitStr);
                /* ==== 输出字符串的连招是：先gep，后putstr ====
                 * 例如：
                 * %i2 = getelementptr [4 x i8], [4 x i8]* @FORMAT_STRING_0, i32 0, i32 0
	             * call void @putstr(i8* %i2)
                 */
                // 作为函数参数，类型应当是i8*，需要降维gep
                Value strParam = IrBuilder.buildRankDownInstruction(IrContext.intBits, strGlobalVar, IrContext.curBlock);
                IrBuilder.buildCallInstruction(Function.putstr, new ArrayList<>(){{
                    add(strParam);
                }}, IrContext.curBlock);
            }
        }
    }
}
