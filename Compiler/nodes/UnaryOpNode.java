package nodes;

import frontend.Token;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 单目运算符
 * @date 2024/10/13 10:54
 * UnaryOp -> '+' | '−' | '!'
 */
public class UnaryOpNode {
    private Token op;
    public UnaryOpNode(Token op) {
        this.op = op;
    }

    public void print() {
        IOUtils.write(op.toString());
        IOUtils.write("<UnaryOp>\n");
    }

    public Token getOp() {
        return op;
    }
}
