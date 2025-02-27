package symbol;

/**
 * @author Gary
 * @Description: 数组符号类：ConstCharArray, ConstIntArray, CharArray, IntArray
 * @date 2024/10/29 16:55
 */
public class ArraySymbol extends Symbol {
    public int size; // 数组大小

    public ArraySymbol(int tableId, String token, SymbolType type) {
        super(tableId, token, type);
    }
}
