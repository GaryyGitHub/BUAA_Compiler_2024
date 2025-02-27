package nodes;

import frontend.Token;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 语句块
 * @date 2024/10/13 10:51
 * Block → '{' { BlockItem } '}'
 */
public class BlockNode {
    private Token leftBrace;
    private List<BlockItemNode> blockItemNodes;
    private Token rightBrace;

    public List<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }

    public Token getRightBrace() {
        return rightBrace;
    }

    public BlockNode(Token leftBrace, List<BlockItemNode> blockItemNodes, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.blockItemNodes = blockItemNodes;
        this.rightBrace = rightBrace;
    }
    
    public void print() {
        IOUtils.write(leftBrace.toString());
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.print();
        }
        IOUtils.write(rightBrace.toString());
        IOUtils.write("<Block>\n");
    }

    // Ir中用到，查看一个block是否以return结尾（不一定与这个block相关！！！）
    public static boolean isReturnEnd(BlockNode blockNode) {
        List<BlockItemNode> blockItemNodes1 = blockNode.getBlockItemNodes();
        if (!blockItemNodes1.isEmpty()) {
            // 取得最后一个item
            BlockItemNode lastItem = blockItemNodes1.get(blockItemNodes1.size() - 1);
            StmtNode lastStmtNode = lastItem.getStmtNode();
            // 该item必须是stmt，并且必须是return类型！
            if (lastStmtNode != null) {
                // 感叹于语法分析的先见之明！！
                return lastStmtNode.getType() == StmtNode.StmtType.RETURN;
            }
        }
        return false;
    }

    // Block → '{' { BlockItem } '}'
    // 每个Block都需要建符号表的，在每个需要调用Block的地方建表即可
    // 有：FuncDef, MainFuncDef, Stmt，push和pop成对出现
    public void buildIr() {
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.buildIr();
        }
    }
}
