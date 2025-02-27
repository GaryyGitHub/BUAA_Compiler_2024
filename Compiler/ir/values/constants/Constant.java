package ir.values.constants;

import ir.types.ValueType;
import ir.values.User;
import ir.values.Value;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 常量类
 * @date 2024/11/18 11:29
 */
public class Constant extends User {
    /**
     * 带有初始值的常量
     * @param type 常量的类型
     * @param operands 常量对应的数组
     */
    public Constant(ValueType type, ArrayList<Value> operands) {
        super("", type, null, operands);
    }

    /**
     * 没有初始值的常量
     * @param type 常量的类型
     */
    public Constant(ValueType type) {
        super("", type, null);
    }

    public String getName() {
        return toString();
    }
}
