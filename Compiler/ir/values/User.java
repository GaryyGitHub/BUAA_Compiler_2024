package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: Value的使用者，本质上也是一个Value。
 * 是一种可以使用其他 Value 对象的 Value 类
 * @date 2024/11/17 16:08
 */
public class User extends Value {
    // ============ 成员变量 ============
    protected ArrayList<Value> operands = new ArrayList<>();

    // ============ getter/setter ============
    public ArrayList<Value> getOperands() {
        return operands;
    }

    /**
     * 获取第index个操作数，注意index从1开始
     * 本质上就是getOperands().get(index-1)
     * @param index 操作数的索引
     * @return  操作数
     */
    public Value getOp(int index) {
        if (operands.size() < index) {
            return null;
        }
        return operands.get(index - 1);
    }

    // ============ 构造函数 ============
    // 默认构造函数
    public User(String name, ValueType type, Value parent) {
        super(name, type, parent);
    }

    /**
     * 带操作对象 values 的初始化
     * @param name 是输出的名称，形如@、%等
     * @param type 类型
     * @param parent
     * @param operands 传入的操作数
     */
    public User(String name, ValueType type, Value parent, ArrayList<Value> operands) {
        super(name, type, parent);
        this.operands.addAll(operands);
        // 绑定被使用者
        for (Value value : operands) {
            if (value != null) {
                value.addUser(this);
            }
        }
    }
}
