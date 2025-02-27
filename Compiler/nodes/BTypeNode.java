package nodes;

import frontend.Token;
import frontend.TokenType;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 基本类型(int, char)。特别注意：<BType>无需输出
 * @date 2024/10/13 10:45
 * BType -> 'int' | 'char'
 */
public class BTypeNode {
    private Token token;

    // 语义分析中，用来获取变量类型
    public TokenType getType() {
        return token.getType();
    }
    public BTypeNode(Token token) {
        this.token = token;
    }
    public void print() {
        IOUtils.write(token.toString());
    }
}
