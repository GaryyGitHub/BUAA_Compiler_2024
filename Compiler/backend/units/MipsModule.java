package backend.units;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Gary
 * @Description: MIPS文件基本单位，包含全局变量和函数
 * @date 2024/11/28 21:59
 */
public class MipsModule {
    // ============ 单例模式 ============
    private static MipsModule instance = new MipsModule();
    private MipsModule() {}
    public static MipsModule getInstance() {
        return instance;
    }

    // ============ 成员变量 ============
    private static ArrayList<MipsGlobalVariable> globalVariables = new ArrayList<>();
    private static ArrayList<MipsFunction> functions = new ArrayList<>();
    private static MipsFunction mainFunction;   // 特别定义的主函数

    // ============ 常用方法 ============

    /**
     * 向全局变量列表中添加全局变量
     */
    public static void addGlobalVariable(MipsGlobalVariable globalVariable) {
        globalVariables.add(globalVariable);
    }

    /**
     * 向函数列表中添加函数
     */
    public static void addFunction(MipsFunction function) {
        if (Objects.equals(function.getName(), "main")) {
            mainFunction = function;
        }
        functions.add(function);
    }

    public static ArrayList<MipsFunction> getFunctions() {
        return functions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 1. 数据段，按照定义顺序输出
        sb.append(".data\n");
        for (MipsGlobalVariable gv : globalVariables) {
            sb.append(gv);
        }
        // 2. 代码段，按顺序输出
        sb.append("\n.text\n");
        // 2.1 主函数
        sb.append(mainFunction);
        // 2.2 其他函数
        for (MipsFunction function : functions) {
            if (!function.isLibFunc() && function != mainFunction) {
                sb.append(function).append("\n");
            }
        }
        return sb.toString();
    }
}
