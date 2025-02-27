package backend.units;

import ir.types.IntType;
import ir.values.constants.ConstInt;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: MIPS代码生成器全局变量类
 * @date 2024/11/28 21:15
 */
public class MipsGlobalVariable {
    // ============ 类型枚举类 ============
    public enum Type {
        String, // 字符串类型
        Zero,   // 未初始化的全局变量，可能是int/char
        Int,    // int型变量or数组，FIXME: char类型要不要单列？
    }
    // ============ 成员变量 ============
    private String name;
    private Type type;

    private int size;       // 变量大小，以字节为单位
    private String stringValue;   // 字符串全局变量的值
    private ArrayList<ConstInt> ints = new ArrayList<>();   // int型全局变量或全局数组的值

    public Type getType() {
        return type;
    }
    // ============ 构造函数 ============
    /**
     * 未初始化的全局数组变量，llvm中使用了zeroinitializer
     */
    public MipsGlobalVariable(String name, int size) {
        // 去掉前缀"@"
        this.name = name.substring(1);
        this.size = size;
        this.type = Type.Zero;
    }

    /**
     * 字符串全局变量
     */
    public MipsGlobalVariable(String name, String stringValue) {
        this.name = name.substring(1);
        this.stringValue = stringValue;
        this.type = Type.String;
    }

    /**
     * int型全局变量或全局数组
     */
    public MipsGlobalVariable(String name, ArrayList<ConstInt> ints) {
        this.name = name.substring(1);
        this.ints.addAll(ints);
        this.type = Type.Int;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(name + ": ");
        switch (type) {
            case Zero -> sb.append(".space\t").append(size).append("\n");
            case String -> sb.append(".asciiz\t\"").append(stringValue).append("\"\n");
            case Int -> {
                // FIXME: 如果是char类型全局变量或数组，先也用.word吧！
                if ( ((IntType)(ints.get(0).getType())).getBits() == 8 ) {
                    sb.append(".word\t");
                    for (ConstInt i : ints) {
                        sb.append(i.getValue()).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length()).append("\n");
                } else {
                    sb.append(".word\t");
                    for (ConstInt i : ints) {
                        sb.append(i.getValue()).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
