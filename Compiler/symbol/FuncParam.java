package symbol;

/**
 * @author Gary
 * @Description: 函数参数符号类，类型有int和char两种
 * @date 2024/10/29 22:29
 */
public class FuncParam {
    private String name;
    private SymbolType type;

    public FuncParam(String name, SymbolType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }
}
