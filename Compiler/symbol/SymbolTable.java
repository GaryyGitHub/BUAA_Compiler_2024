package symbol;

import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description:
 * @date 2024/10/29 10:52
 */
public class SymbolTable {
    public int id;          // 当前符号表的id
    public int fatherId; 	// 外层符号表的id。
    public List<Symbol> directory; // 当前符号表目录
    public SymbolType type; // 若该符号表是函数符号表，则记录其返回类型。

    public List<Symbol> getDirectory() {
        return directory;
    }
    public SymbolType getType() {
        return type;
    }

    public SymbolTable(int id, int fatherId, SymbolType type) {
        this.id = id;
        this.fatherId = fatherId;
        this.type = type;
        this.directory = new ArrayList<>();
    }

    public void printResult() {
        for (Symbol symbol : directory) {
            IOUtils.write(symbol.toString());
        }
    }
}
