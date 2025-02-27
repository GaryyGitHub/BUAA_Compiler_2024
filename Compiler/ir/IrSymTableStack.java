package ir;

import java.util.Stack;
import ir.values.Value;

/**
 * @author Gary
 * @Description: IrSymTable的栈类，存了两个重要变量：
 * 1. stack：符号表栈，CAUTION：这是符号表IrSymTable组成的栈，谁都存！
 * 2. globalSymTable：全局符号表，只用来存储全局变量、函数，是一个IrSymTable
 * @date 2024/11/17 16:54
 */
public class IrSymTableStack {
    private static IrSymTableStack instance = new IrSymTableStack();
    // ============ 变量定义 ============
    // 符号表栈，CAUTION：这是符号表IrSymTable组成的栈，谁都存！
    private final Stack<IrSymTable> stack = new Stack<>();
    // 全局符号表，只用来存储全局变量、函数，是一个IrSymTable
    public static IrSymTable globalSymTable;

    // ============ 全局符号表操作 ============
    public static void addGlobalSymTable(String name, Value value) {
        globalSymTable.addSymbol(name, value);
    }

    // ========== 以下为栈操作 ==========
    // 初始化符号表栈：清空栈，并将全局符号表压入栈顶
    public static void init() {
        instance.stack.clear();
        globalSymTable = new IrSymTable();
        instance.stack.push(globalSymTable);
    }

    // 访问栈顶的符号表
    public static IrSymTable peek() {
        if (instance.stack.isEmpty()) {
            return null;
        }
        return instance.stack.peek();
    }

    /**
     * 向栈顶符号表添加元素！注意：这个是向栈顶的符号表加东西，不是把新符号表压栈
     * 只是一个IrSymTable的addSymbol方法的封装
     */
    public static void addSymToPeek(String name, Value value) {
        peek().addSymbol(name, value);
    }

    // 判断是否正在构建全局符号表
    public static boolean isBuildingGlobalSymTable() {
        return instance.stack.size() == 1;
    }

    // 压栈一个给定的符号表
    public static void push(IrSymTable symTable) {
        instance.stack.push(symTable);
    }

    // 创建一个新的符号表并压栈
    public static IrSymTable push() {
        IrSymTable symTable = new IrSymTable();
        push(symTable);
        return symTable;
    }

    // 栈顶符号表出栈，但全局符号表并不会出栈！
    public static void pop() {
        if(instance.stack.size() > 1) {
            instance.stack.pop();
        }
    }

    // 寻找栈中指定name的符号，返回它的value
    public static Value getSymbol(String name) {
        // 从栈顶开始查找
        for (int i=instance.stack.size()-1; i>=0; i--) {
            Value value = instance.stack.get(i).getSymbol(name);
            if(value != null) {
                return value;
            }
        }
        return null;
    }
}
