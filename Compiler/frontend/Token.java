package frontend;

/**
 * @author Gary
 * @Description: 词法分析中的标识符类
 * @date 2024/9/24 13:57
 */
public class Token {
    //
    private TokenType type;
    private String value;
    private int lineNum;
    public Token(TokenType type, String value, int lineNum) {
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
    }
    public TokenType getType() {
        return type;
    }
    public String getValue() {
        return value;
    }
    public int getLineNum() {
        return lineNum;
    }
    // 词法分析输出
    public String toString() {
        return type.toString() + " " + value + "\n";
    }
}
