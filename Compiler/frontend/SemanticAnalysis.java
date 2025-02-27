package frontend;

import error.ErrorHandler;
import error.ErrorType;
import error.MyError;
import nodes.*;
import symbol.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 语义分析器，负责建立符号表和错误处理，单例模式
 * @date 2024/10/29 17:34
 */
public class SemanticAnalysis {
    private static SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
    public static SemanticAnalysis getInstance() {
        return semanticAnalysis;
    }
    /** 下面进行语义分析的错误处理 **/
    // 各作用域符号表列表
    private List<SymbolTable> symbolTables = new ArrayList<>();

    public void printResult() {
        for (SymbolTable symbolTable : symbolTables) {
            symbolTable.printResult();
        }
    }

    // 进入新作用域：将当前作用域符号表加入总表
    private void newScope(SymbolType scopeType) {
        // 特性：出现子作用域时scopeCount++。type是如果符号表为函数，填入IntFunc或CharFunc
        symbolTables.add(new SymbolTable(++scopeCount, curScope, scopeType));
        curScope = scopeCount;
    }
    // 离开当前作用域：虽然不移除符号表，但需要更新当前作用域
    private void exitScope() {
        curScope = symbolTables.get(curScope - 1).fatherId;
    }

    private void putSymbol(Symbol symbol) {
        symbolTables.get(curScope - 1).getDirectory().add(symbol);
    }

    // 根据标识符名获取符号，从当前作用域开始向上查找
    private Symbol getSymbol(String ident) {
//        for (int i = curScope - 1; i >= 0; i--) {
//            Symbol symbol = symbolTables.get(i).getDirectory().get(ident);
//            if (symbol != null) {
//                return symbol;
//            }
//        }
        int id = curScope;
        while (id >= 1) {
            Symbol returnSymbol = null;
            for (Symbol symbol : symbolTables.get(id - 1).getDirectory()) {
                if (symbol.token.equals(ident)) {
                    returnSymbol = symbol;
                }
            }
            if (returnSymbol != null) return returnSymbol;
            id = symbolTables.get(id - 1).fatherId;
        }
        return null;
    }

    // 检查当前作用域符号表是否存在某变量名（String, 而非Token）
    private boolean inCurrentScope(String varName) {
        List<Symbol> symbols = symbolTables.get(curScope - 1).getDirectory();
        for (Symbol symbol : symbols) {
            if (symbol.token.equals(varName)) {
                return true;
            }
        }
        return false;
    }
    private int loopCount = 0;
    private int scopeCount = 0;
    private int curScope = 0;
    // 建立好ErrorHandler的单例
    private ErrorHandler errorHandler = ErrorHandler.getInstance();
    // CompUnit → {Decl} {FuncDef} MainFuncDef
    public void CompUnit(CompUnitNode compUnitNode) {
        newScope(null);
        for (DeclNode declNode : compUnitNode.getDeclNodes()) {
            Decl(declNode);
        }
        for (FuncDefNode funcDefNode : compUnitNode.getFuncDefNodes()) {
            FuncDef(funcDefNode);
        }
        MainFuncDef(compUnitNode.getMainFuncDefNode());
    }

    // Decl → ConstDecl | VarDecl
    private void Decl(DeclNode declNode) {
        if (declNode.getConstDeclNode() != null) {
            ConstDecl(declNode.getConstDeclNode());
        } else {
            VarDecl(declNode.getVarDeclNode());
        }
    }

    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private void ConstDecl(ConstDeclNode constDeclNode) {
        // 鉴于ConstDef仍需要BTypeNode，所以调用ConstDef要传BType
        BTypeNode bTypeNode = constDeclNode.getBTypeNode();
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            ConstDef(constDefNode, bTypeNode);
        }
    }

    // 像BType这样的不涉及非终结符就不用写了
    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b
    private void ConstDef(ConstDefNode constDefNode, BTypeNode bTypeNode) {
        // 检查当前作用域是否存在同名常量，若出现错误b，则不再进行符号表操作
        if(inCurrentScope(constDefNode.getIdent().getValue())) {
            errorHandler.addErrorTable(new MyError(constDefNode.getIdent().getLineNum(), ErrorType.b));
            return;
        }
        // 是个数组，则需要创建ArraySymbol
        if (constDefNode.getConstExp() != null) {
            ConstExp(constDefNode.getConstExp());
            if (bTypeNode.getType() == TokenType.INTTK)
                putSymbol(new ArraySymbol(curScope, constDefNode.getIdent().getValue(), SymbolType.ConstIntArray));
            else  // CHARTK
                putSymbol(new ArraySymbol(curScope, constDefNode.getIdent().getValue(), SymbolType.ConstCharArray));
        } else {  // 不是数组，则直接创建Symbol
            if (bTypeNode.getType() == TokenType.INTTK)
                putSymbol(new VarSymbol(curScope, constDefNode.getIdent().getValue(), SymbolType.ConstInt));
            else  // CHARTK
                putSymbol(new VarSymbol(curScope, constDefNode.getIdent().getValue(), SymbolType.ConstChar));
        }
        ConstInitVal(constDefNode.getConstInitValNode());
    }

    // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
    private void ConstInitVal(ConstInitValNode constInitValNode) {
        if (constInitValNode.getLeftBrace() == null) {
            // 情况1：只有一个常量表达式
            // FIXME: 此处不能写成constInitValNode.getConstExpNodes() != null，因为ConstExpNodes被初始化了，不会是null
            if (constInitValNode.getStringConst() == null) {
                ConstExp(constInitValNode.getConstExpNodes().get(0));
            }
            // 情况2：只有一个字符串常量，不处理
        } else {
            // 情况3：有多个常量表达式，逐个处理
            for (ConstExpNode constExpNode : constInitValNode.getConstExpNodes()) {
                ConstExp(constExpNode);
            }
        }
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    private void VarDecl(VarDeclNode varDeclNode) {
        BTypeNode bTypeNode = varDeclNode.getBTypeNode();
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            VarDef(varDefNode, bTypeNode);
        }
    }

    // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // b
    private void VarDef(VarDefNode varDefNode, BTypeNode bTypeNode) {
        // 检查当前作用域是否存在同名变量，若出现错误b，则不再进行符号表操作
        if (inCurrentScope(varDefNode.getIdent().getValue())) {
            errorHandler.addErrorTable(new MyError(varDefNode.getIdent().getLineNum(), ErrorType.b));
            return;
        }
        // 是个数组，则需要创建ArraySymbol
        if (varDefNode.getConstExpNode() != null) {
            ConstExp(varDefNode.getConstExpNode());
            if (bTypeNode.getType() == TokenType.INTTK)
                putSymbol(new ArraySymbol(curScope, varDefNode.getIdent().getValue(), SymbolType.IntArray));
            else  // CHARTK
                putSymbol(new ArraySymbol(curScope, varDefNode.getIdent().getValue(), SymbolType.CharArray));
        } else {  // 不是数组，则直接创建Symbol
            if (bTypeNode.getType() == TokenType.INTTK)
                putSymbol(new VarSymbol(curScope, varDefNode.getIdent().getValue(), SymbolType.Int));
            else  // CHARTK
                putSymbol(new VarSymbol(curScope, varDefNode.getIdent().getValue(), SymbolType.Char));
        }
        // 处理初始化值
        if (varDefNode.getInitValNode() != null) {
            InitVal(varDefNode.getInitValNode());
        }
    }

    // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
    private void InitVal(InitValNode initValNode) {
        if (initValNode.getLeftBrace() == null) {
            // 情况1：只有一个表达式
            // FIXME: 此处不能写成initValNode.getExpNodes() != null，因为这个ExpNodes被初始化了，不会是null
            if (initValNode.getStringConst() == null) {
                Exp(initValNode.getExpNodes().get(0));
            }
            // 情况2：只有一个字符串常量，不处理
        } else {
            // 情况3：有多个表达式，逐个处理
            for (ExpNode expNode : initValNode.getExpNodes()) {
                Exp(expNode);
            }
        }
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g
    private void FuncDef(FuncDefNode funcDefNode) {
        if (inCurrentScope(funcDefNode.getIdent().getValue())) {
            // b类错误
            errorHandler.addErrorTable(new MyError(funcDefNode.getIdent().getLineNum(), ErrorType.b));
            return;
        }
        // 重要！g类错误推迟到Block中处理！！！
        SymbolType type;    // 函数类型
        if (funcDefNode.getFuncTypeNode().getType() == TokenType.VOIDTK) type = SymbolType.VoidFunc;
        else if (funcDefNode.getFuncTypeNode().getType() == TokenType.INTTK) type = SymbolType.IntFunc;
        else type = SymbolType.CharFunc;
        if (funcDefNode.getFuncFParamsNode() == null) {
            // 没有参数，则直接创建FuncSymbol，参数列表就是空的
            putSymbol(new FuncSymbol(curScope, funcDefNode.getIdent().getValue(), type, new ArrayList<>()));
        } else {
            // 有参数，则先创建参数列表
            List<FuncParam> params = new ArrayList<>();
            // 遍历参数列表，每个参数是一个funcFParamNode
            for (FuncFParamNode funcFParamNode : funcDefNode.getFuncFParamsNode().getFuncFParamNodes()) {
                if (funcFParamNode.getBTypeNode().getType() == TokenType.INTTK) {
                    if (funcFParamNode.getLeftBracket() != null)
                        params.add(new FuncParam(funcFParamNode.getIdent().getValue(), SymbolType.IntArray));
                    else
                        params.add(new FuncParam(funcFParamNode.getIdent().getValue(), SymbolType.Int));
                }
                else { // CHARTK
                    if (funcFParamNode.getLeftBracket() != null)
                        params.add(new FuncParam(funcFParamNode.getIdent().getValue(), SymbolType.CharArray));
                    else
                        params.add(new FuncParam(funcFParamNode.getIdent().getValue(), SymbolType.Char));
                }
            }
            putSymbol(new FuncSymbol(curScope, funcDefNode.getIdent().getValue(), type, params));
        }
        // 进入函数体，需要处理下一层作用域
        newScope(type);
        // 首先把函数形参加入该作用域符号表
        if (funcDefNode.getFuncFParamsNode() != null) {
            FuncFParams(funcDefNode.getFuncFParamsNode());
        }
        Block(funcDefNode.getBlockNode());
        exitScope();
    }

    // MainFuncDef → 'int' 'main' '(' ')' Block // g
    private void MainFuncDef(MainFuncDefNode mainFuncDefNode) {
        // main函数是IntFunc类型
        newScope(SymbolType.IntFunc);
        Block(mainFuncDefNode.getBlockNode());
        exitScope();
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    private void FuncFParams(FuncFParamsNode funcFParamsNode) {
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            FuncFParam(funcFParamNode);
        }
    }

    // FuncFParam → BType Ident ['[' ']'] // b
    private void FuncFParam(FuncFParamNode funcFParamNode) {
        if (inCurrentScope(funcFParamNode.getIdent().getValue())) {
            // b类错误
            errorHandler.addErrorTable(new MyError(funcFParamNode.getIdent().getLineNum(), ErrorType.b));
            return;
        }
        // 是个数组，则需要创建ArraySymbol
        if (funcFParamNode.getLeftBracket() != null) {
            if (funcFParamNode.getBTypeNode().getType() == TokenType.INTTK)
                putSymbol(new ArraySymbol(curScope, funcFParamNode.getIdent().getValue(), SymbolType.IntArray));
            else  // CHARTK
                putSymbol(new ArraySymbol(curScope, funcFParamNode.getIdent().getValue(), SymbolType.CharArray));
        } else {  // 不是数组，则直接创建Symbol
            if (funcFParamNode.getBTypeNode().getType() == TokenType.INTTK)
                putSymbol(new VarSymbol(curScope, funcFParamNode.getIdent().getValue(), SymbolType.Int));
            else  // CHARTK
                putSymbol(new VarSymbol(curScope, funcFParamNode.getIdent().getValue(), SymbolType.Char));
        }
    }

    // Block → '{' { BlockItem } '}'
    private void Block(BlockNode blockNode) {
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            BlockItem(blockItemNode);
        }
        // 处理各类有返回值函数的g类错误：缺少末尾的return。强制末尾有return，尽管实际并不强制
        if (symbolTables.get(curScope - 1).getType() == SymbolType.IntFunc || symbolTables.get(curScope - 1).getType() == SymbolType.CharFunc) {
            if (blockNode.getBlockItemNodes().isEmpty() ||
                blockNode.getBlockItemNodes().get(blockNode.getBlockItemNodes().size() - 1).getStmtNode() == null ||
                blockNode.getBlockItemNodes().get(blockNode.getBlockItemNodes().size() - 1).getStmtNode().getReturnToken() == null ) {
                // 报错行号为函数结尾的’}’所在行号
                errorHandler.addErrorTable(new MyError(blockNode.getRightBrace().getLineNum(), ErrorType.g));
            }
        }
    }

    // BlockItem → Decl | Stmt
    private void BlockItem(BlockItemNode blockItemNode) {
        if (blockItemNode.getDeclNode() != null) {
            Decl(blockItemNode.getDeclNode());
        } else {
            Stmt(blockItemNode.getStmtNode());
        }
    }

    /* Stmt -> -LVal '=' Exp ';' // h
        -| [Exp] ';'
        -| Block
        -| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        -| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt` // h
        -| 'break' ';' | 'continue' ';'  // m
        -| 'return' [Exp] ';' // f
        -| LVal '=' 'getint''('')'';' // h
        -| LVal '=' 'getchar''('')'';' // h
        -| 'printf''('StringConst {','Exp}')'';' // l
     */
    private void Stmt(StmtNode stmtNode) {
        switch (stmtNode.getType()) {
            case ASSIGN:
                // LVal '=' Exp ';' // h
                LVal(stmtNode.getLValNode());
                Exp(stmtNode.getExpNode());
                Symbol symbol = getSymbol(stmtNode.getLValNode().getIdent().getValue());
                if(symbol != null){
                    if (symbol.type == SymbolType.ConstChar || symbol.type == SymbolType.ConstInt ||
                            symbol.type == SymbolType.ConstIntArray || symbol.type == SymbolType.ConstCharArray) {
                        errorHandler.addErrorTable(new MyError(stmtNode.getLValNode().getIdent().getLineNum(), ErrorType.h));
                    }
                }
                break;
            case GETINT:
            case GETCHAR:
                // LVal '=' 'getint''('')'';' // h
                // LVal '=' 'getchar''('')'';' // h
                LVal(stmtNode.getLValNode());
                Symbol symbol1 = getSymbol(stmtNode.getLValNode().getIdent().getValue());
                if (symbol1 != null) {
                    if (symbol1.type == SymbolType.ConstChar || symbol1.type == SymbolType.ConstInt ||
                            symbol1.type == SymbolType.ConstIntArray || symbol1.type == SymbolType.ConstCharArray) {
                        errorHandler.addErrorTable(new MyError(stmtNode.getLValNode().getIdent().getLineNum(), ErrorType.h));
                    }
                }
                break;
            case EXP:
                // [Exp] ';'
                if (stmtNode.getExpNode() != null) {
                    Exp(stmtNode.getExpNode());
                }
                break;
            case BLOCK:
                // Block
                newScope(null);
                Block(stmtNode.getBlockNode());
                exitScope();
                break;
            case IF:
                // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                Cond(stmtNode.getCondNode());
                Stmt(stmtNode.getStmtNodes().get(0));
                if (stmtNode.getElseToken() != null) {
                    Stmt(stmtNode.getStmtNodes().get(1));
                }
                break;
            case FOR:
                // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt` // h
                if (stmtNode.getForStmtNode1() != null) {
                    ForStmt(stmtNode.getForStmtNode1());
                }
                if (stmtNode.getCondNode() != null) {
                    Cond(stmtNode.getCondNode());
                }
                if (stmtNode.getForStmtNode2() != null) {
                    ForStmt(stmtNode.getForStmtNode2());
                }
                loopCount++;
                Stmt(stmtNode.getStmtNodes().get(0));
                loopCount--;
                break;
            case BREAK:
            case CONTINUE:
                // 'break' ';' | 'continue' ';'  // m
                if (loopCount == 0) {
                    errorHandler.addErrorTable(new MyError(stmtNode.getBreakContinueToken().getLineNum(), ErrorType.m));
                }
                break;
            case RETURN:
                // 'return' [Exp] ';' // f
                // 为判断f类错误，需要从当前作用域往上找最近的函数，看是否有返回值声明
                int id = curScope;      // 维护当前作用域的id
                while (id >= 1) {
//                    System.out.println("id = " + id);
                    // 找到最近的void函数，return语句后面还有exp，报错
                    if (symbolTables.get(id - 1).getType() == SymbolType.VoidFunc && stmtNode.getExpNode() != null) {
                        errorHandler.addErrorTable(new MyError(stmtNode.getReturnToken().getLineNum(), ErrorType.f));
                    }
                    // 更新id为父作用域的id
                    id = symbolTables.get(id - 1).fatherId;
                }
//                System.out.println("end");
                if (stmtNode.getExpNode() != null) {
                    Exp(stmtNode.getExpNode());
                }
                break;
            case PRINTF:
                // 'printf''('StringConst {','Exp}')'';' // l
                int expCount = stmtNode.getExpNodes().size();
                int formatCount = 0;
                for (int i = 0; i < stmtNode.getStringConstToken().getValue().length(); i++) {
                    // 统计格式化字符串中的%d或%c的个数
                    if (stmtNode.getStringConstToken().getValue().charAt(i) == '%') {
                        if (stmtNode.getStringConstToken().getValue().charAt(i + 1) == 'd' || stmtNode.getStringConstToken().getValue().charAt(i + 1) == 'c') {
                            formatCount++;
                        }
                    }
                }
                if (expCount != formatCount) {
                    errorHandler.addErrorTable(new MyError(stmtNode.getPrintfToken().getLineNum(), ErrorType.l));
                }
                for (ExpNode expNode : stmtNode.getExpNodes()) {
                    Exp(expNode);
                }
                break;
        }
    }

    // ForStmt → LVal '=' Exp // h
    private void ForStmt(ForStmtNode forStmtNode) {
        LVal(forStmtNode.getLValNode());
        Exp(forStmtNode.getExpNode());
        Symbol symbol = getSymbol(forStmtNode.getLValNode().getIdent().getValue());
        if (symbol.type == SymbolType.ConstChar || symbol.type == SymbolType.ConstInt ||
                symbol.type == SymbolType.ConstIntArray || symbol.type == SymbolType.ConstCharArray) {
            errorHandler.addErrorTable(new MyError(forStmtNode.getLValNode().getIdent().getLineNum(), ErrorType.h));
        }
    }

    // Exp → AddExp
    private void Exp(ExpNode expNode) {
        AddExp(expNode.getAddExpNode());
    }

    private FuncParam exp2Param (ExpNode expNode) {
        return addExp2Param(expNode.getAddExpNode());
    }

    // Cond → LOrExp
    private void Cond(CondNode condNode) {
        LOrExp(condNode.getLOrExpNode());
    }

    // LVal → Ident ['[' Exp ']'] // c
    private void LVal(LValNode lValNode) {
        // 当前作用域及父作用域是否存在该变量，不存在则报c类错误
        if (getSymbol(lValNode.getIdent().getValue()) == null) {
            errorHandler.addErrorTable(new MyError(lValNode.getIdent().getLineNum(), ErrorType.c));
            return;
        }
        if (lValNode.getExpNode() != null) {
            Exp(lValNode.getExpNode());
        }
    }

    // LVal → Ident ['[' Exp ']'] // c
    private FuncParam lVal2Param (LValNode lValNode) {
        Symbol symbol = getSymbol(lValNode.getIdent().getValue());
        SymbolType type = symbol.type;
        if (lValNode.getExpNode() != null) {
            if(symbol.type == SymbolType.ConstCharArray || symbol.type == SymbolType.CharArray)
                type = SymbolType.Char;
            else if(symbol.type == SymbolType.ConstIntArray || symbol.type == SymbolType.IntArray)
                type = SymbolType.Int;
        }
        return new FuncParam(lValNode.getIdent().getValue(), type);
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number | Character
    private void PrimaryExp(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getExpNode() != null) {
            Exp(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getLVal() != null) {
            LVal(primaryExpNode.getLVal());
        }
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number | Character
    private FuncParam primaryExp2Param(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getExpNode() != null) {
            return exp2Param(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getLVal() != null) {
            return lVal2Param(primaryExpNode.getLVal());
        } else if (primaryExpNode.getNumberNode() != null) {
            return new FuncParam(null, SymbolType.Int);
        } else {
            return new FuncParam(null, SymbolType.Char);
        }
    }

    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e
    private void UnaryExp(UnaryExpNode unaryExpNode) {
        // PrimaryExp
        if (unaryExpNode.getPrimaryExpNode() != null) {
            PrimaryExp(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getUnaryExpNode() != null) {    // UnaryOp UnaryExp
            UnaryExp(unaryExpNode.getUnaryExpNode());
        } else {    // Ident '(' [FuncRParams] ')'
            if (getSymbol(unaryExpNode.getIdent().getValue()) == null) {
                errorHandler.addErrorTable(new MyError(unaryExpNode.getIdent().getLineNum(), ErrorType.c));
                return;
            }
            Symbol symbol = getSymbol(unaryExpNode.getIdent().getValue());
            if (!(symbol instanceof FuncSymbol)) {
                errorHandler.addErrorTable(new MyError(unaryExpNode.getIdent().getLineNum(), ErrorType.e));
                return;
            }
            FuncSymbol funcSymbol = (FuncSymbol) symbol;
            // 如果函数没有参数，则理应参数列表为空
            if (unaryExpNode.getFuncRParamsNode() == null) {
                if (funcSymbol.funcParams.size() != 0) {
                    // 参数不为空，报d类错误
                    errorHandler.addErrorTable(new MyError(unaryExpNode.getIdent().getLineNum(), ErrorType.d));
                }
            } else { // 函数有参数，检查参数个数是否匹配、参数类型是否匹配
                if (funcSymbol.funcParams.size() != unaryExpNode.getFuncRParamsNode().getExpNodes().size()) {
                    // 参数个数不匹配，报d类错误
                    errorHandler.addErrorTable(new MyError(unaryExpNode.getIdent().getLineNum(), ErrorType.d));
                    return;
                }
                // 接下来检测是否有类型不匹配的情况
                FuncRParams(unaryExpNode.getFuncRParamsNode());
                // TODO: 检查参数类型是否匹配
                List<FuncParam> definitionParams = funcSymbol.funcParams;
                List<FuncParam> callParams = new ArrayList<>();
                // 下面把调用函数的exp类型参数列表转化为FuncParam列表
//                for (ExpNode expNode : unaryExpNode.getFuncRParamsNode().getExpNodes()) {
//                    FuncParam funcParam = exp2Param(expNode);
//                }
                for (int i = 0; i < unaryExpNode.getFuncRParamsNode().getExpNodes().size(); i++) {
                    ExpNode expNode = unaryExpNode.getFuncRParamsNode().getExpNodes().get(i);
                    FuncParam callParam = exp2Param(expNode);
//                    System.out.println(unaryExpNode.getIdent().getLineNum());
//                    System.out.println("callParam = " + callParam.getName() + " " + callParam.getType());
//                    System.out.println("definitionParams = " + definitionParams.get(i).getName() + " " + definitionParams.get(i).getType());
                    FuncParam definitionParam = definitionParams.get(i);
                    if(!(callParam.getType() == definitionParam.getType() ||
                            (callParam.getType() == SymbolType.Int && definitionParam.getType() == SymbolType.ConstInt) ||
                            (callParam.getType() == SymbolType.ConstInt && definitionParam.getType() == SymbolType.Int) ||
                            (callParam.getType() == SymbolType.Char && definitionParam.getType() == SymbolType.ConstChar) ||
                            (callParam.getType() == SymbolType.ConstChar && definitionParam.getType() == SymbolType.Char) ||
                            (callParam.getType() == SymbolType.IntArray && definitionParam.getType() == SymbolType.ConstIntArray) ||
                            (callParam.getType() == SymbolType.ConstIntArray && definitionParam.getType() == SymbolType.IntArray) ||
                            (callParam.getType() == SymbolType.ConstCharArray && definitionParam.getType() == SymbolType.CharArray) ||
                            (callParam.getType() == SymbolType.CharArray && definitionParam.getType() == SymbolType.ConstCharArray) ||
                            (callParam.getType() == SymbolType.Int && definitionParam.getType() == SymbolType.Char) ||
                            (callParam.getType() == SymbolType.Int && definitionParam.getType() == SymbolType.ConstChar) ||
                            (callParam.getType() == SymbolType.Char && definitionParam.getType() == SymbolType.Int) ||
                            (callParam.getType() == SymbolType.Char && definitionParam.getType() == SymbolType.ConstInt) ||
                            (callParam.getType() == SymbolType.ConstChar && definitionParam.getType() == SymbolType.ConstInt) ||
                            (callParam.getType() == SymbolType.ConstChar && definitionParam.getType() == SymbolType.Int) ||
                            (callParam.getType() == SymbolType.ConstInt && definitionParam.getType() == SymbolType.ConstChar) ||
                            (callParam.getType() == SymbolType.ConstInt && definitionParam.getType() == SymbolType.Char))) {
                        errorHandler.addErrorTable(new MyError(unaryExpNode.getIdent().getLineNum(), ErrorType.e));
                        break;
                    }
                }
            }
        }
    }

    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e
    private FuncParam unaryExp2Param (UnaryExpNode unaryExpNode) {
        if (unaryExpNode.getPrimaryExpNode() != null) {
            return primaryExp2Param(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getIdent() != null) {
            // FIXME: 可能有NullPointerException，注意一下
            SymbolType type = getSymbol(unaryExpNode.getIdent().getValue()).type;
            if (type == SymbolType.IntFunc) type = SymbolType.Int;
            else if (type == SymbolType.CharFunc) type = SymbolType.Char;
            return new FuncParam(unaryExpNode.getIdent().getValue(), type);
        } else {
            return unaryExp2Param(unaryExpNode.getUnaryExpNode());
        }
    }

    // FuncRParams → Exp { ',' Exp }
    private void FuncRParams(FuncRParamsNode funcRParamsNode) {
        for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
            Exp(expNode);
        }
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 11.22修改：以前按照右递归文法写，但Ir时出现问题，不得不重构成左递归
    private void MulExp(MulExpNode mulExpNode) {
//        UnaryExp(mulExpNode.getUnaryExpNode());
//        if (mulExpNode.getMulExpNode() != null) {
//            MulExp(mulExpNode.getMulExpNode());
//        }
        if (mulExpNode.getMulExpNode() == null) {
            UnaryExp(mulExpNode.getUnaryExpNode());
        } else {
            MulExp(mulExpNode.getMulExpNode());
            UnaryExp(mulExpNode.getUnaryExpNode());
        }
    }

    private FuncParam mulExp2Param (MulExpNode mulExpNode) {
        return unaryExp2Param(mulExpNode.getUnaryExpNode());
    }

    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    private void AddExp(AddExpNode addExpNode) {
//        MulExp(addExpNode.getMulExpNode());
//        if (addExpNode.getAddExpNode() != null) {
//            AddExp(addExpNode.getAddExpNode());
//        }
        if (addExpNode.getAddExpNode() == null) {
            MulExp(addExpNode.getMulExpNode());
        } else {
            AddExp(addExpNode.getAddExpNode());
            MulExp(addExpNode.getMulExpNode());
        }
    }

    private FuncParam addExp2Param (AddExpNode addExpNode) {
        return mulExp2Param(addExpNode.getMulExpNode());
    }

    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private void RelExp(RelExpNode relExpNode) {
//        AddExp(relExpNode.getAddExpNode());
//        if (relExpNode.getRelExpNode() != null) {
//            RelExp(relExpNode.getRelExpNode());
//        }
        if (relExpNode.getRelExpNode() == null) {
            AddExp(relExpNode.getAddExpNode());
        } else {
            RelExp(relExpNode.getRelExpNode());
            AddExp(relExpNode.getAddExpNode());
        }
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private void EqExp(EqExpNode eqExpNode) {
//        RelExp(eqExpNode.getRelExpNode());
//        if (eqExpNode.getEqExpNode() != null) {
//            EqExp(eqExpNode.getEqExpNode());
//        }
        if (eqExpNode.getEqExpNode() == null) {
            RelExp(eqExpNode.getRelExpNode());
        } else {
            EqExp(eqExpNode.getEqExpNode());
            RelExp(eqExpNode.getRelExpNode());
        }
    }

    // LAndExp → EqExp | LAndExp '&&' EqExp
    private void LAndExp(LAndExpNode lAndExpNode) {
//        EqExp(lAndExpNode.getEqExpNode());
//        if (lAndExpNode.getLAndExpNode() != null) {
//            LAndExp(lAndExpNode.getLAndExpNode());
//        }
        if (lAndExpNode.getLAndExpNode() == null) {
            EqExp(lAndExpNode.getEqExpNode());
        } else {
            LAndExp(lAndExpNode.getLAndExpNode());
            EqExp(lAndExpNode.getEqExpNode());
        }
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp
    private void LOrExp(LOrExpNode lOrExpNode) {
//        LAndExp(lOrExpNode.getLAndExpNode());
//        if (lOrExpNode.getLOrExpNode() != null) {
//            LOrExp(lOrExpNode.getLOrExpNode());
//        }
        if (lOrExpNode.getLOrExpNode() == null) {
            LAndExp(lOrExpNode.getLAndExpNode());
        } else {
            LOrExp(lOrExpNode.getLOrExpNode());
            LAndExp(lOrExpNode.getLAndExpNode());
        }
    }

    // ConstExp → AddExp
    private void ConstExp(ConstExpNode constExpNode) {
        AddExp(constExpNode.getAddExpNode());
    }
}
