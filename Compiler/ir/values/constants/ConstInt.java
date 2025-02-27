package ir.values.constants;

import ir.types.IntType;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 整数常量类
 * @date 2024/11/18 11:38
 */
public class ConstInt extends Constant {
    // value 就是整数常量的值
    private int value;

    // 获取整数常量的值
    public int getValue() {
        return value;
    }

    public ConstInt(int intBits, int value) {
        super(new IntType(intBits), new ArrayList<>());
        this.value = value;
    }

    /**
     * 构建一个intBits位的0值ConstInt对象
     * @param intBits   int类型位数，可为8或32
     * @return  ConstInt对象
     */
    public static ConstInt ZERO(int intBits) {
        return new ConstInt(intBits, 0);
    }

    public String toString() {
        return "" + value;
    }
}
