package symbol;

/**
 * @author Gary
 * @Description: 普通变量符号类：ConstChar, ConstInt, Char, Int
 * @date 2024/10/29 16:54
 */
public class VarSymbol extends Symbol {
    public VarSymbol(int tableId, String token, SymbolType type) {
        super(tableId, token, type);
    }
}
