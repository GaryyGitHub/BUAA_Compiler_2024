package symbol;

/**
 * @author Gary
 * @Description: 符号类
 * @date 2024/10/29 9:28
 */
public abstract class Symbol {
    public int tableId; 	// 当前单词所在的符号表编号。
    public String token; 	// 当前单词所对应的字符串。
    public SymbolType type; 	// 符号类型
    public Symbol(int tableId, String token, SymbolType type) {
        this.tableId = tableId;
        this.token = token;
        this.type = type;
    }

    @Override
    public String toString() {
        return tableId + " " + token + " " + type + "\n";
    }
}
