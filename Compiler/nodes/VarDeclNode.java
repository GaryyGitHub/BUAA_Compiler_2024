package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrContext;
import utils.IOUtils;

import java.util.List;

/**
 * @author Gary
 * @Description: 变量声明
 * @date 2024/10/13 10:47
 * VarDecl -> BType VarDef { ',' VarDef } ';'
 */
public class VarDeclNode {
    private BTypeNode bTypeNode;
    private List<VarDefNode> varDefNodes;
    private List<Token> commas;
    private Token semicolon;

    public BTypeNode getBTypeNode() {
        return bTypeNode;
    }

    public List<VarDefNode> getVarDefNodes() {
        return varDefNodes;
    }

    public VarDeclNode(BTypeNode bTypeNode, List<VarDefNode> varDefNodes, List<Token> commas, Token semicolon) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commas = commas;
        this.semicolon = semicolon;
    }

    public void print() {
        bTypeNode.print();
        varDefNodes.get(0).print();
        for (int i = 1; i < varDefNodes.size(); i++) {
            IOUtils.write(commas.get(i-1).toString());
            varDefNodes.get(i).print();
        }
        IOUtils.write(semicolon.toString());
        IOUtils.write("<VarDecl>\n");
    }

    public void buildIr() {
        for (VarDefNode varDefNode : varDefNodes) {
            // 先处理类型信息, Gary自创的，需要接收BTypeNode作为参数以判断是int还是char
            TokenType bTypeNodeType = bTypeNode.getType();
            IrContext.intBits = (bTypeNodeType == TokenType.INTTK) ? 32 : 8;
            varDefNode.buildIr();
        }
    }
}
