package frontend;

import error.ErrorHandler;
import error.ErrorType;
import error.MyError;
import nodes.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 语法分析器，单例模式
 * @date 2024/10/12 23:56
 */
public class Parser {
    private static Parser instance = new Parser();
    public static Parser getInstance() { return instance; }
    // 语法分析器的输入：词法单元流
    private List<Token> inputTokens;
    private int curPos = 0;     // 这个变量只有当遇到终结符时才会更新！！！
    private Token curToken;    // 和curPos对应，只有当遇到非终结符时才会更新
    public void setTokens(List<Token> inputTokens) {
        this.inputTokens = inputTokens;
        // 初始化，设置第一个Token
        curToken = inputTokens.get(curPos);
    }
    private CompUnitNode entry;
    // 语法分析，入口是CompUnit
    public void analyze() {
        this.entry = CompUnit();
    }
    // 打印结果就是CompUnit的打印程序
    public void printResult() {
        entry.print();
    }
    // newAdded: 获取语法树的根节点，在语义分析中用到
    public CompUnitNode getEntry() {
        return entry;
    }
    // 只有当遇到终结符时才会被调用，更新curPos和curToken，并且判断当前终结符是否符合预期
    public Token judge(TokenType type) {
        ErrorHandler errorHandler = ErrorHandler.getInstance();
        int errorLineNum = curPos > 0? inputTokens.get(curPos-1).getLineNum() : 1;
        if (curToken.getType() == type) {
            // 当前token的类型符合预期，返回当前Token，并更新curPos和curToken
            Token returnToken = curToken;
            if (curPos < inputTokens.size()-1) {
                curToken = inputTokens.get(++curPos);
            }
            return returnToken;
        } else if (type == TokenType.SEMICN) {
            // 不符合预期，且预期符号是分号，报错i，行号为分号前一个非终结符所在行号。
            if (recallFlag == 0)    // 只在没有回溯的时候才出现报错！！！
                errorHandler.addErrorTable(new MyError(errorLineNum, ErrorType.i));
            // 纠错，把这个符号改为分号，然后继续分析
            return new Token(TokenType.SEMICN, ";", errorLineNum);
        } else if (type == TokenType.RPARENT) {  // 报错j
//            System.out.println("jjjjj");
            if (recallFlag == 0)
                errorHandler.addErrorTable(new MyError(errorLineNum, ErrorType.j));
            return new Token(TokenType.RPARENT, ")", errorLineNum);
        } else if (type == TokenType.RBRACK) {  // 报错k
            if (recallFlag == 0)
                errorHandler.addErrorTable(new MyError(errorLineNum, ErrorType.k));
            return new Token(TokenType.RBRACK, "]", errorLineNum);
        } else {
            throw new RuntimeException("Unexpected token at LINE " + curToken.getLineNum() + ", value: " + curToken.getValue() + " should be " + type);
        }
    }

    /* 递归子程序法进行语法分析 */
    // CompUnit -> {Decl} {FuncDef} MainFuncDef，需要预读、回溯
    private CompUnitNode CompUnit() {
        // 务必先赋初值
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode = null;
        // 遍历。考虑decl的特性：第二项不会是main，第三项不会是左括号
        while (inputTokens.get(curPos+1).getType() != TokenType.MAINTK && inputTokens.get(curPos+2).getType() != TokenType.LPARENT) {
            DeclNode declNode = Decl();
            declNodes.add(declNode);
        }
        // funcDef的特性：第二项不会是main，第三项一定是左括号
        while (inputTokens.get(curPos+1).getType() != TokenType.MAINTK) {
            FuncDefNode funcDefNode = FuncDef();
            funcDefNodes.add(funcDefNode);
        }
        mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    // Decl -> ConstDecl | VarDecl
    private DeclNode Decl() {
        ConstDeclNode constDeclNode = null;
        VarDeclNode varDeclNode = null;
        if (curToken.getType() == TokenType.CONSTTK) {
            constDeclNode = ConstDecl();
        } else {
            varDeclNode = VarDecl();
        }
        return new DeclNode(constDeclNode, varDeclNode);
    }

    // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDeclNode ConstDecl() {
        Token constToken = judge(TokenType.CONSTTK);
        BTypeNode bTypeNode = BType();
        List<ConstDefNode> constDefNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        Token semicnToken = null;
        constDefNodes.add(ConstDef());
        while (curToken.getType() == TokenType.COMMA) {
            commaTokens.add(judge(TokenType.COMMA));
            constDefNodes.add(ConstDef());
        }
        semicnToken = judge(TokenType.SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, constDefNodes, commaTokens, semicnToken);
    }

    // BType -> 'int' | 'char'
    private BTypeNode BType() {
        Token token;
        if (curToken.getType() == TokenType.INTTK) {
            token = judge(TokenType.INTTK);
        } else {
            token = judge(TokenType.CHARTK);
        }
        return new BTypeNode(token);
    }

    // ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitVal
    private ConstDefNode ConstDef() {
        Token ident = judge(TokenType.IDENFR);
        Token leftBracket = null, rightBracket = null, equalSign = null;
        ConstExpNode constExpNode = null;
        if (curToken.getType() == TokenType.LBRACK) {
            leftBracket = judge(TokenType.LBRACK);
            constExpNode = ConstExp();
            rightBracket = judge(TokenType.RBRACK);
        }
        equalSign = judge(TokenType.ASSIGN);
        ConstInitValNode constInitValNode = ConstInitVal();
        return new ConstDefNode(ident, leftBracket, constExpNode, rightBracket, equalSign, constInitValNode);
    }

    // ConstInitVal -> ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
    private ConstInitValNode ConstInitVal() {
        List<ConstExpNode> constExpNodes = new ArrayList<>();
        Token leftBrace = null, rightBrace = null, stringConst = null;
        List<Token> commaTokens = new ArrayList<>();
        if (curToken.getType() == TokenType.LBRACE) {
            leftBrace = judge(TokenType.LBRACE);
            if (curToken.getType() != TokenType.RBRACE) {
                constExpNodes.add(ConstExp());
                while (curToken.getType() != TokenType.RBRACE) {
                    commaTokens.add(judge(TokenType.COMMA));
                    constExpNodes.add(ConstExp());
                }
            }
            rightBrace = judge(TokenType.RBRACE);
        } else if (curToken.getType() == TokenType.STRCON) {
            stringConst = judge(TokenType.STRCON);
        } else {
            constExpNodes.add(ConstExp());
        }
        return new ConstInitValNode(constExpNodes, leftBrace, rightBrace, commaTokens, stringConst);
    }

    // VarDecl -> BType VarDef { ',' VarDef } ';'
    private VarDeclNode VarDecl() {
        BTypeNode bTypeNode = BType();
        List<VarDefNode> varDefNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        Token semicnToken = null;
        varDefNodes.add(VarDef());
        while (curToken.getType() == TokenType.COMMA) {
            commaTokens.add(judge(TokenType.COMMA));
            varDefNodes.add(VarDef());
        }
        semicnToken = judge(TokenType.SEMICN);
        return new VarDeclNode(bTypeNode, varDefNodes, commaTokens, semicnToken);
    }

    // VarDef -> Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
    private VarDefNode VarDef() {
        Token ident = judge(TokenType.IDENFR);
        Token leftBracket = null, rightBracket = null, assign = null;
        ConstExpNode constExpNode = null;
        InitValNode initValNode = null;
        if (curToken.getType() == TokenType.LBRACK) {
            leftBracket = judge(TokenType.LBRACK);
            constExpNode = ConstExp();
            rightBracket = judge(TokenType.RBRACK);
        }
        if (curToken.getType() == TokenType.ASSIGN) {
            assign = judge(TokenType.ASSIGN);
            initValNode = InitVal();
        }
        return new VarDefNode(ident, leftBracket, constExpNode, rightBracket, assign, initValNode);
    }

    // InitVal -> Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
    private InitValNode InitVal() {
        List<ExpNode> expNodes = new ArrayList<>();
        Token leftBrace = null, rightBrace = null, stringConst = null;
        List<Token> commaTokens = new ArrayList<>();
        if (curToken.getType() == TokenType.LBRACE) {
            leftBrace = judge(TokenType.LBRACE);
            if (curToken.getType() != TokenType.RBRACE) {
                expNodes.add(Exp());
                while (curToken.getType() != TokenType.RBRACE) {
                    commaTokens.add(judge(TokenType.COMMA));
                    expNodes.add(Exp());
                }
            }
            rightBrace = judge(TokenType.RBRACE);
        } else if (curToken.getType() == TokenType.STRCON) {
            stringConst = judge(TokenType.STRCON);
        } else {
            expNodes.add(Exp());
        }
        return new InitValNode(expNodes, leftBrace, rightBrace, commaTokens, stringConst);
    }

    // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDefNode FuncDef() {
        FuncTypeNode funcTypeNode = FuncType();
        Token ident = judge(TokenType.IDENFR);
        Token leftParent = judge(TokenType.LPARENT);
        FuncFParamsNode funcFParamsNode = null;
        if (curToken.getType() == TokenType.INTTK || curToken.getType() == TokenType.CHARTK) {
            funcFParamsNode = FuncFParams();
        }
        Token rightParent = judge(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new FuncDefNode(funcTypeNode, ident, leftParent, funcFParamsNode, rightParent, blockNode);
    }

    // MainFuncDef -> 'int' 'main' '(' ')' Block
    private MainFuncDefNode MainFuncDef() {
        Token intToken = judge(TokenType.INTTK);
        Token mainToken = judge(TokenType.MAINTK);
        Token leftParent = judge(TokenType.LPARENT);
        Token rightParent = judge(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new MainFuncDefNode(intToken, mainToken, leftParent, rightParent, blockNode);
    }

    // FuncType -> 'void' | 'int' | 'char'
    private FuncTypeNode FuncType() {
        Token token;
        if (curToken.getType() == TokenType.VOIDTK) {
            token = judge(TokenType.VOIDTK);
        } else if (curToken.getType() == TokenType.INTTK) {
            token = judge(TokenType.INTTK);
        } else {
            token = judge(TokenType.CHARTK);
        }
        return new FuncTypeNode(token);
    }

    // FuncFParams -> FuncFParam { ',' FuncFParam }
    private FuncFParamsNode FuncFParams() {
        List<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        funcFParamNodes.add(FuncFParam());
        while (curToken.getType() == TokenType.COMMA) {
            commaTokens.add(judge(TokenType.COMMA));
            funcFParamNodes.add(FuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes, commaTokens);
    }

    // FuncFParam -> BType Ident ['[' ']']
    private FuncFParamNode FuncFParam() {
        BTypeNode bTypeNode = BType();
        Token ident = judge(TokenType.IDENFR);
        Token leftBracket = null, rightBracket = null;
        if (curToken.getType() == TokenType.LBRACK) {
            leftBracket = judge(TokenType.LBRACK);
            rightBracket = judge(TokenType.RBRACK);
        }
        return new FuncFParamNode(bTypeNode, ident, leftBracket, rightBracket);
    }

    // Block → '{' { BlockItem } '}'
    private BlockNode Block() {
        Token leftBrace = judge(TokenType.LBRACE);
        List<BlockItemNode> blockItemNodes = new ArrayList<>();
        while (curToken.getType() != TokenType.RBRACE) {
            blockItemNodes.add(BlockItem());
        }
        Token rightBrace = judge(TokenType.RBRACE);
        return new BlockNode(leftBrace, blockItemNodes, rightBrace);
    }

    // BlockItem -> Decl | Stmt
    private BlockItemNode BlockItem() {
        DeclNode declNode = null;
        StmtNode stmtNode = null;
        // 预读，通过下一个token的类型判断下一个token是Decl还是Stmt
        if (curToken.getType() == TokenType.CONSTTK || curToken.getType() == TokenType.INTTK || curToken.getType() == TokenType.CHARTK) {
            declNode = Decl();
        } else {
            stmtNode = Stmt();
        }
        return new BlockItemNode(declNode, stmtNode);
    }

    /* Stmt -> LVal '=' Exp ';'
        -| [Exp] ';'
        -| Block
        -| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        -| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt`
        -| 'break' ';'
        -| 'continue' ';'
        -| 'return' [Exp] ';'
        -| LVal '=' 'getint''('')'';'
        -| LVal '=' 'getchar''('')'';'
        -| 'printf''('StringConst {','Exp}')'';'
     */
    private StmtNode Stmt() {
        if(curToken.getType() == TokenType.IFTK) {
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Token ifToken = judge(TokenType.IFTK);
            Token leftParent = judge(TokenType.LPARENT);
            CondNode condNode = Cond();
            Token rightParent = judge(TokenType.RPARENT);
            List<StmtNode> stmtNodes = new ArrayList<>();
            stmtNodes.add(Stmt());
            Token elseToken = null;
            if (curToken.getType() == TokenType.ELSETK) {
                elseToken = judge(TokenType.ELSETK);
                stmtNodes.add(Stmt());
            }
            return new StmtNode(StmtNode.StmtType.IF, ifToken, leftParent, condNode, rightParent, stmtNodes, elseToken);
        } else if (curToken.getType() == TokenType.FORTK) {
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt`
            Token forToken = judge(TokenType.FORTK);
            Token leftParent = judge(TokenType.LPARENT);
            ForStmtNode forStmtNode1 = null;
            if (curToken.getType() != TokenType.SEMICN) {
                forStmtNode1 = ForStmt();
            }
            Token semicn1 = judge(TokenType.SEMICN);
            CondNode condNode = null;
            if (curToken.getType() != TokenType.SEMICN) {
                condNode = Cond();
            }
            Token semicn2 = judge(TokenType.SEMICN);
            ForStmtNode forStmtNode2 = null;
            if (curToken.getType() != TokenType.RPARENT) {
                forStmtNode2 = ForStmt();
            }
            Token rightParent = judge(TokenType.RPARENT);
            List<StmtNode> stmtNodes = new ArrayList<>();
            stmtNodes.add(Stmt());
            return new StmtNode(StmtNode.StmtType.FOR, forToken, leftParent, forStmtNode1, semicn1, condNode, semicn2, forStmtNode2, rightParent, stmtNodes);
        } else if (curToken.getType() == TokenType.BREAKTK) {
            // 'break' ';'
            Token breakToken = judge(TokenType.BREAKTK);
            Token semicn = judge(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.BREAK, breakToken, semicn);
        } else if (curToken.getType() == TokenType.CONTINUETK) {
            // 'continue' ';'
            Token continueToken = judge(TokenType.CONTINUETK);
            Token semicn = judge(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.CONTINUE, continueToken, semicn);
        } else if (curToken.getType() == TokenType.RETURNTK) {
            // 'return' [Exp] ';'
            Token returnToken = judge(TokenType.RETURNTK);
            ExpNode expNode = null;
            // bugfix: 这里不能用SEMICN判断！因为有可能是return后忘跟分号了
            if (expJudge()) {
                expNode = Exp();
            }
            Token semicn = judge(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.RETURN, returnToken, expNode, semicn);
        } else if (curToken.getType() == TokenType.PRINTFTK) {
            // 'printf''('StringConst {','Exp}')'';'
            Token printfToken = judge(TokenType.PRINTFTK);
            Token leftParent = judge(TokenType.LPARENT);
            Token stringConst = judge(TokenType.STRCON);
            List<Token> commaTokens = new ArrayList<>();
            List<ExpNode> expNodes = new ArrayList<>();
            while (curToken.getType() == TokenType.COMMA) {
                commaTokens.add(judge(TokenType.COMMA));
                expNodes.add(Exp());
            }
            Token rightParent = judge(TokenType.RPARENT);
            Token semicn = judge(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.PRINTF, printfToken, leftParent, stringConst, commaTokens, expNodes, rightParent, semicn);
        } else if (curToken.getType() == TokenType.LBRACE) {
            // Block
            BlockNode blockNode = Block();
            return new StmtNode(StmtNode.StmtType.BLOCK, blockNode);
        } else {
            // LVal '=' Exp ';' | LVal '=' 'getint''('')'';' | LVal '=' 'getchar''('')'';' | [Exp] ';'
            // 为分号，';'，无Exp，则无需进行任何操作
            if (curToken.getType() == TokenType.SEMICN) {
                Token semicn = judge(TokenType.SEMICN);
                ExpNode expNode = null;
                return new StmtNode(StmtNode.StmtType.EXP, expNode, semicn);
            } else {
                // 先使用Exp消去LVal和Exp
                savePos1();
                recallFlag = 1;
//                System.out.println("start " + curToken);
                ExpNode expNode = Exp();
                recallFlag = 0;
//                System.out.println("end " + curToken);
                // 为分号，Exp ';'，有Exp
                if (curToken.getType() == TokenType.SEMICN) {
                    restorePos1();  // 这里还是回到原来的位置吧，方便错误处理判断是否处于回溯状态
                    expNode = Exp();
                    Token semicn = judge(TokenType.SEMICN);
                    return new StmtNode(StmtNode.StmtType.EXP, expNode, semicn);
                } else {
                    // 非分号，那么有两种情况：1、是LVal开头的语句
                    if(curToken.getType() == TokenType.ASSIGN) {
                        restorePos1();
                        LValNode lValNode = LVal();
                        Token assign = judge(TokenType.ASSIGN);
                        if (curToken.getType() == TokenType.GETINTTK) {
                            // LVal '=' 'getint''('')'';'
                            Token getintToken = judge(TokenType.GETINTTK);
                            Token leftParent = judge(TokenType.LPARENT);
                            Token rightParent = judge(TokenType.RPARENT);
                            Token semicn = judge(TokenType.SEMICN);
                            return new StmtNode(StmtNode.StmtType.GETINT, lValNode, assign, getintToken, leftParent, rightParent, semicn);
                        } else if (curToken.getType() == TokenType.GETCHARTK) {
                            // LVal '=' 'getchar''('')'';'
                            Token getcharToken = judge(TokenType.GETCHARTK);
                            Token leftParent = judge(TokenType.LPARENT);
                            Token rightParent = judge(TokenType.RPARENT);
                            Token semicn = judge(TokenType.SEMICN);
                            return new StmtNode(StmtNode.StmtType.GETCHAR, lValNode, assign, getcharToken, leftParent, rightParent, semicn);
                        } else {
                            // LVal '=' Exp ';'
                            expNode = Exp();    // FIXME: 不知道有没有问题
                            Token semicn = judge(TokenType.SEMICN);
                            return new StmtNode(StmtNode.StmtType.ASSIGN, lValNode, assign, expNode, semicn);
                        }
                    } else {
                        // 2、Exp后面忘跟分号了
                        System.out.println("Exp后面忘跟分号了，此报错不在judge中 " + inputTokens.get(curPos-1) + "line: " + inputTokens.get(curPos-1).getLineNum());
                        ErrorHandler.getInstance().addErrorTable(new MyError(inputTokens.get(curPos-1).getLineNum(), ErrorType.i));
                        return new StmtNode(StmtNode.StmtType.EXP, expNode, new Token(TokenType.SEMICN, ";", inputTokens.get(curPos-1).getLineNum()));
                    }

                }
            }

            /* 不能用这个昏招！！！考虑这个样例：f(); a=1;
            * 通过等号位置判断是不能处理上述情况的！
            * */
            // 条件：若后面的token有ASSIGN，则为左值赋值语句，否则为exp（要特别注意LVal可能为数组！不能简单地认为curpos+1就是=号）
//            int assignPos = curPos;
//            for (int i = curPos; i < inputTokens.size() && inputTokens.get(i).getLineNum() == curToken.getLineNum(); i++) {
//                if (inputTokens.get(i).getType() == TokenType.ASSIGN) assignPos = i;
//            }
////            if (inputTokens.get(curPos + 1).getType() == TokenType.ASSIGN) {
//            if (assignPos > curPos) {
//                LValNode lValNode = LVal();
//                Token assign = judge(TokenType.ASSIGN);
//                if (curToken.getType() == TokenType.GETINTTK) {
//                    Token getintToken = judge(TokenType.GETINTTK);
//                    Token leftParent = judge(TokenType.LPARENT);
//                    Token rightParent = judge(TokenType.RPARENT);
//                    Token semicn = judge(TokenType.SEMICN);
//                    return new StmtNode(StmtNode.StmtType.GETINT, lValNode, assign, getintToken, leftParent, rightParent, semicn);
//                } else if (curToken.getType() == TokenType.GETCHARTK) {
//                    Token getcharToken = judge(TokenType.GETCHARTK);
//                    Token leftParent = judge(TokenType.LPARENT);
//                    Token rightParent = judge(TokenType.RPARENT);
//                    Token semicn = judge(TokenType.SEMICN);
//                    return new StmtNode(StmtNode.StmtType.GETCHAR, lValNode, assign, getcharToken, leftParent, rightParent, semicn);
//                } else {
//                    ExpNode expNode = Exp();
//                    Token semicn = judge(TokenType.SEMICN);
//                    return new StmtNode(StmtNode.StmtType.ASSIGN, lValNode, assign, expNode, semicn);
//                }
//            } else {
//                // [Exp] ';'
//                ExpNode expNode = null;
//                // bugfix: bugfix: 这里不能用SEMICN判断！因为有可能是exp后忘跟分号了
//                if (expJudge()) {
//                    expNode = Exp();
//                }
//                Token semicn = judge(TokenType.SEMICN);
//                return new StmtNode(StmtNode.StmtType.EXP, expNode, semicn);
//            }
        }
    }

    // 标志是否处于回溯状态
    int recallFlag = 0;
    // 保存回溯前的位置
    int savedPos1 = 0;
    private void savePos1() {
        savedPos1 = curPos;
    }
    // 回溯到保存的位置
    private void restorePos1() {
        curPos = savedPos1;
        curToken = inputTokens.get(curPos);
    }

    private boolean expJudge() {
        return curToken.getType() == TokenType.IDENFR ||
                curToken.getType() == TokenType.INTCON ||
                curToken.getType() == TokenType.CHRCON ||
                curToken.getType() == TokenType.LPARENT ||
                curToken.getType() == TokenType.PLUS ||
                curToken.getType() == TokenType.MINU ||
                curToken.getType() == TokenType.NOT;
    }

    // ForStmt -> LVal '=' Exp
    private ForStmtNode ForStmt() {
        LValNode lValNode = LVal();
        Token assign = judge(TokenType.ASSIGN);
        ExpNode expNode = Exp();
        return new ForStmtNode(lValNode, assign, expNode);
    }

    // Exp -> AddExp
    private ExpNode Exp() {
        return new ExpNode(AddExp());
    }

    // Cond -> LOrExp
    private CondNode Cond() {
        return new CondNode(LOrExp());
    }

    // LVal -> Ident ['[' Exp ']']
    private LValNode LVal() {
        Token ident = judge(TokenType.IDENFR);
        Token leftBracket = null, rightBracket = null;
        ExpNode expNode = null;
        if (curToken.getType() == TokenType.LBRACK) {
            leftBracket = judge(TokenType.LBRACK);
            expNode = Exp();
            rightBracket = judge(TokenType.RBRACK);
        }
        return new LValNode(ident, leftBracket, expNode, rightBracket);
    }

    // PrimaryExp -> '(' Exp ')' | LVal | Number | Character
    private PrimaryExpNode PrimaryExp() {
        if (curToken.getType() == TokenType.LPARENT) {
            Token leftParent = judge(TokenType.LPARENT);
            ExpNode expNode = Exp();
            Token rightParent = judge(TokenType.RPARENT);
            return new PrimaryExpNode(leftParent, expNode, rightParent);
        } else if (curToken.getType() == TokenType.INTCON) {
            NumberNode numberNode = Number();
            return new PrimaryExpNode(numberNode);
        } else if (curToken.getType() == TokenType.CHRCON) {
            CharacterNode characterNode = Character();
            return new PrimaryExpNode(characterNode);
        } else {
            LValNode lValNode = LVal();
            return new PrimaryExpNode(lValNode);
        }
    }

    // Number -> IntConst
    private NumberNode Number() {
        Token intConst = judge(TokenType.INTCON);
        return new NumberNode(intConst);
    }

    // Character -> CharConst
    private CharacterNode Character() {
        Token charConst = judge(TokenType.CHRCON);
        return new CharacterNode(charConst);
    }

    // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private UnaryExpNode UnaryExp() {
        if (curToken.getType() == TokenType.IDENFR && inputTokens.get(curPos + 1).getType() == TokenType.LPARENT) {
            // Ident '(' [FuncRParams] ')'
            Token ident = judge(TokenType.IDENFR);
            Token leftParent = judge(TokenType.LPARENT);
            FuncRParamsNode funcRParamsNode = null;
            // bugfix: 这里不能用RPARENT判断！因为有可能是函数调用后忘跟右括号了
            // if (curToken.getType() != TokenType.RPARENT) {
            if (expJudge()) {
                funcRParamsNode = FuncRParams();
            }
            Token rightParent = judge(TokenType.RPARENT);
            return new UnaryExpNode(ident, leftParent, funcRParamsNode, rightParent);
        } else if (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU || curToken.getType() == TokenType.NOT) {
            // UnaryOp UnaryExp
            UnaryOpNode unaryOpNode = UnaryOp();
            UnaryExpNode unaryExpNode = UnaryExp();
            return new UnaryExpNode(unaryOpNode, unaryExpNode);
        } else {
            // PrimaryExp
            PrimaryExpNode primaryExpNode = PrimaryExp();
            return new UnaryExpNode(primaryExpNode);
        }
    }

    // UnaryOp -> '+' | '−' | '!'
    private UnaryOpNode UnaryOp() {
        Token token;
        if (curToken.getType() == TokenType.PLUS) {
            token = judge(TokenType.PLUS);
        } else if (curToken.getType() == TokenType.MINU) {
            token = judge(TokenType.MINU);
        } else {
            token = judge(TokenType.NOT);
        }
        return new UnaryOpNode(token);
    }

    // FuncRParams -> Exp { ',' Exp }
    private FuncRParamsNode FuncRParams() {
        List<ExpNode> expNodes = new ArrayList<>();
        List<Token> commaTokens = new ArrayList<>();
        expNodes.add(Exp());
        while (curToken.getType() == TokenType.COMMA) {
            commaTokens.add(judge(TokenType.COMMA));
            expNodes.add(Exp());
        }
        return new FuncRParamsNode(expNodes, commaTokens);
    }

    // MulExp -> UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private MulExpNode MulExp() {
        // 11.22注：以前：按照右递归文法写，但Ir时出现问题，不得不重构！
        UnaryExpNode unaryExpNode = UnaryExp();
        Token op = null;
        MulExpNode mulExpNode = null;
//        if(curToken.getType() == TokenType.MULT || curToken.getType() == TokenType.DIV || curToken.getType() == TokenType.MOD) {
//            op = judge(curToken.getType());
//            mulExpNode = MulExp();
//        }
        // 存在('*' | '/' | '%') 那么捕获外层结构，然后组装回一层层的MulExp
        while (curToken.getType() == TokenType.MULT || curToken.getType() == TokenType.DIV || curToken.getType() == TokenType.MOD) {
            // 将上一轮捕获的单位进行组装
            mulExpNode = new MulExpNode(unaryExpNode, op, mulExpNode);
            op = judge(curToken.getType());
            unaryExpNode = UnaryExp();
        }
        return new MulExpNode(unaryExpNode, op, mulExpNode);
    }

    // AddExp -> MulExp | AddExp ('+' | '−') MulExp
    private AddExpNode AddExp() {
        MulExpNode mulExpNode = MulExp();
        Token op = null;
        AddExpNode addExpNode = null;
//        if(curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU) {
//            op = judge(curToken.getType());
//            addExpNode = AddExp();
//        }
        // 存在('+' | '−') 那么捕获外层结构，然后组装回一层层的AddExp
        while (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU) {
            // 将上一轮捕获的单位进行组装
            addExpNode = new AddExpNode(mulExpNode, op, addExpNode);
            op = judge(curToken.getType());
            mulExpNode = MulExp();
        }
        return new AddExpNode(mulExpNode, op, addExpNode);
    }

    // RelExp -> AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private RelExpNode RelExp() {
        AddExpNode addExpNode = AddExp();
        Token op = null;
        RelExpNode relExpNode = null;
//        if(curToken.getType() == TokenType.LSS || curToken.getType() == TokenType.GRE || curToken.getType() == TokenType.LEQ || curToken.getType() == TokenType.GEQ) {
//            op = judge(curToken.getType());
//            relExpNode = RelExp();
//        }
        // 存在('<' | '>' | '<=' | '>=') 那么捕获外层结构，然后组装回一层层的RelExp
        while (curToken.getType() == TokenType.LSS || curToken.getType() == TokenType.GRE || curToken.getType() == TokenType.LEQ || curToken.getType() == TokenType.GEQ) {
            // 将上一轮捕获的单位进行组装
            relExpNode = new RelExpNode(addExpNode, op, relExpNode);
            op = judge(curToken.getType());
            addExpNode = AddExp();
        }
        return new RelExpNode(addExpNode, op, relExpNode);
    }

    // EqExp -> RelExp | EqExp ('==' | '!=') RelExp
    private EqExpNode EqExp() {
        RelExpNode relExpNode = RelExp();
        Token op = null;
        EqExpNode eqExpNode = null;
//        if(curToken.getType() == TokenType.EQL || curToken.getType() == TokenType.NEQ) {
//            op = judge(curToken.getType());
//            eqExpNode = EqExp();
//        }
        // 存在('==' | '!=') 那么捕获外层结构，然后组装回一层层的EqExp
        while (curToken.getType() == TokenType.EQL || curToken.getType() == TokenType.NEQ) {
            // 将上一轮捕获的单位进行组装
            eqExpNode = new EqExpNode(relExpNode, op, eqExpNode);
            op = judge(curToken.getType());
            relExpNode = RelExp();
        }
        return new EqExpNode(relExpNode, op, eqExpNode);
    }

    // LAndExp -> EqExp | LAndExp '&&' EqExp
    private LAndExpNode LAndExp() {
        EqExpNode eqExpNode = EqExp();
        Token op = null;
        LAndExpNode lAndExpNode = null;
//        if(curToken.getType() == TokenType.AND) {
//            op = judge(TokenType.AND);
//            lAndExpNode = LAndExp();
//        }
        // 存在('&&') 那么捕获外层结构，然后组装回一层层的LAndExp
        while (curToken.getType() == TokenType.AND) {
            // 将上一轮捕获的单位进行组装
            lAndExpNode = new LAndExpNode(eqExpNode, op, lAndExpNode);
            op = judge(TokenType.AND);
            eqExpNode = EqExp();
        }
        return new LAndExpNode(eqExpNode, op, lAndExpNode);
    }

    // LOrExp -> LAndExp | LOrExp '||' LAndExp
    private LOrExpNode LOrExp() {
        LAndExpNode lAndExpNode = LAndExp();
        Token op = null;
        LOrExpNode lOrExpNode = null;
//        if (curToken.getType() == TokenType.OR) {
//            op = judge(TokenType.OR);
//            lOrExpNode = LOrExp();
//        }
        // 存在('||') 那么捕获外层结构，然后组装回一层层的LOrExp
        while (curToken.getType() == TokenType.OR) {
            // 将上一轮捕获的单位进行组装
            lOrExpNode = new LOrExpNode(lAndExpNode, op, lOrExpNode);
            op = judge(TokenType.OR);
            lAndExpNode = LAndExp();
        }
        return new LOrExpNode(lAndExpNode, op, lOrExpNode);
    }

    // ConstExp -> AddExp
    private ConstExpNode ConstExp() {
        return new ConstExpNode(AddExp());
    }
}
