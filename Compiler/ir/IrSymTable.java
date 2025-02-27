package ir;

import ir.values.Value;

import java.util.HashMap;

/**
 * @author Gary
 * @Description: LLVM IR阶段的符号表，和语义分析我搞的类似
 * @date 2024/11/17 16:54
 */
public class IrSymTable {
    // LLVM IR阶段的符号表
    private HashMap<String, Value> symbolMap = new HashMap<>();

    // 向符号表里添加符号
    public void addSymbol(String name, Value value) {
        symbolMap.put(name, value);
    }

    // 寻炸指定name的符号，返回它的value
    public Value getSymbol(String name) {
        return symbolMap.get(name);
    }
}
