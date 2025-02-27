package nodes;

/**
 * @author Gary
 * @Description: 语句块项。特别注意：<BlockItem>无需输出
 * @date 2024/10/13 10:51
 * BlockItem -> Decl | Stmt
 */
public class BlockItemNode {
    private DeclNode declNode;
    private StmtNode stmtNode;

    public DeclNode getDeclNode() {
        return declNode;
    }

    public StmtNode getStmtNode() {
        return stmtNode;
    }

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        this.declNode = declNode;
        this.stmtNode = stmtNode;
    }

    public void print() {
        if (declNode != null) {
            declNode.print();
        } else {
            stmtNode.print();
        }
    }

    // BlockItem -> Decl | Stmt
    public void buildIr() {
        if (declNode != null) {
            declNode.buildIr();
        } else {
            stmtNode.buildIr();
        }
    }
}
