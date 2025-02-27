package nodes;

import frontend.Token;
import utils.IOUtils;

import java.util.List;

/**
 * @author Gary
 * @Description: 函数实参表
 * @date 2024/10/13 10:54
 * FuncRParams -> Exp { ',' Exp }
 */
public class FuncRParamsNode {
    private List<ExpNode> expNodes;
    private List<Token> commas;

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }

    public FuncRParamsNode(List<ExpNode> expNodes, List<Token> commas) {
        this.expNodes = expNodes;
        this.commas = commas;
    }

    public void print() {
        expNodes.get(0).print();
        for (int i = 1; i < expNodes.size(); i++) {
            IOUtils.write(commas.get(i-1).toString());
            expNodes.get(i).print();
        }
        IOUtils.write("<FuncRParams>\n");
    }
}
