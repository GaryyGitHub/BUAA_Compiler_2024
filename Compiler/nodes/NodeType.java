package nodes;

/**
 * @author Gary
 * @Description: 抽象语法树-结点类型枚举类
 * @date 2024/10/13 0:22
 */
public enum NodeType {
    CompUnit,
    Decl,
    ConstDecl,
    BType,
    ConstDef,
    ConstInitVal,
    VarDecl,
    VarDef,
    InitVal,
    FuncDef,
    MainFuncDef,
    FuncType,
    FuncFParams,
    FuncFParam,
    Block,
    BlockItem,
    Stmt,
    ForStmt,
    Exp,
    Cond,
    LVal,
    PrimaryExp,
    Number,
    Character,
    UnaryExp,
    UnaryOp,
    FuncRParams,
    MulExp,
    AddExp,
    RelExp,
    EqExp,
    LAndExp,
    LOrExp,
    ConstExp,
}
