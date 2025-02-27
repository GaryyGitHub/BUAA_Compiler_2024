package nodes;

/**
 * @author Gary
 * @Description: 声明。特别注意：<Decl>无需输出
 * @date 2024/10/13 0:45
 * Decl -> ConstDecl | VarDecl
 */
public class DeclNode {
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode, VarDeclNode varDeclNode) {
        this.constDeclNode = constDeclNode;
        this.varDeclNode = varDeclNode;
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }

    public void print() {
        // 由于是constDecl和varDecl是或的关系，需要用if-else来判断
        if (constDeclNode != null) {
            constDeclNode.print();
        } else {
            varDeclNode.print();
        }
    }

    // 中间代码生成 Decl -> ConstDecl | VarDecl
    public void buildIr() {
        if (constDeclNode != null) {
            constDeclNode.buildIr();
        } else {
            varDeclNode.buildIr();
        }
    }
}
