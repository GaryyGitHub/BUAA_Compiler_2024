package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrContext;
import utils.IOUtils;

import java.util.List;

/**
 * @author Gary
 * @Description: 常量声明
 * @date 2024/10/13 10:44
 * ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
 */
public class ConstDeclNode {
    private Token constToken;
    private BTypeNode bTypeNode;
    private List<ConstDefNode> constDefNodes;
    private List<Token> commaTokens; // 用来存逗号
    private Token semicnToken;

    public BTypeNode getBTypeNode() {
        return bTypeNode;
    }

    public List<ConstDefNode> getConstDefNodes() {
        return constDefNodes;
    }

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, List<ConstDefNode> constDefNodes, List<Token> commaTokens, Token semicnToken) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commaTokens = commaTokens;
        this.semicnToken = semicnToken;
    }

    public void print() {
        IOUtils.write(constToken.toString());
        bTypeNode.print();
        constDefNodes.get(0).print();
        for (int i = 1; i < constDefNodes.size(); i++) {
            IOUtils.write(commaTokens.get(i - 1).toString());
            constDefNodes.get(i).print();
        }
        IOUtils.write(semicnToken.toString());
        IOUtils.write("<ConstDecl>\n");
    }

    // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
    public void buildIr() {
        for (ConstDefNode constDefNode : constDefNodes) {
            // 先处理类型信息, Gary自创的，需要接收BTypeNode作为参数以判断是int还是char
            TokenType bTypeNodeType = bTypeNode.getType();
            IrContext.intBits = (bTypeNodeType == TokenType.INTTK) ? 32 : 8;
            constDefNode.buildIr();
        }
    }
}
