package symbol;

import java.util.List;
/**
 * @author Gary
 * @Description: 函数符号类：VoidFunc, CharFunc, IntFunc
 * @date 2024/10/29 17:05
 */
public class FuncSymbol extends Symbol {
    public List<FuncParam> funcParams; // 参数类型列表

    public FuncSymbol(int tableId, String token, SymbolType type, List<FuncParam> funcParams) {
        super(tableId, token, type);
        this.funcParams = funcParams;
    }
}
