package ir.values.constants;

import ir.types.ValueType;

/**
 * @author Gary
 * @Description: 为数组的每个元素分配一个零值常量
 * 作为一个llvm指令，特别让它继承constant类，以便数组赋初常量值
 * @date 2024/11/18 11:38
 */
public class ZeroInitializer extends Constant {
    public ZeroInitializer(ValueType type) {
        super(type);
    }

    public String toString() {
        return "zeroinitializer";
    }
}
