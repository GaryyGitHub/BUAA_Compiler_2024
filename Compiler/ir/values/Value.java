package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Gary
 * @Description: 基本的Value类，是所有东西的父类。一切皆value
 * @date 2024/11/17 16:08
 */
public class Value {
    // ============== 基本属性 ==============
    private int id;         // 唯一标识符
    private String name;    // 虚拟寄存器名称
    private ValueType type; // 类型
    private Value parent;   // 包含当前value的父value，如Instruction在BasicBlock中
    private boolean isArg = false;  // 是否是函数参数，默认不是
    private int argId = 0;     // 第几个参数，从0开始
    private ArrayList<User> users = new ArrayList<>();  // 使用当前value的User列表

    private static int idCnt = 0;   // 用于生成唯一标识符
    // 唯一标识符按顺序增加
    private static int applyNewId() {
        return idCnt++;
    }

    // ============== getter/setter ==============
    public ValueType getType() {
        return type;
    }

    public Value getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    // 返回不带前缀的变量名，只在MipsBuilder.buildGVOperand中使用
    public String getNameCnt() {
        return name.substring(1);
    }

    // 返回是否为函数参数，只在MipsBuilder.buildOperand中使用
    public boolean isArg() {
        return isArg;
    }

    // 返回参数序号，只在MipsBuilder.buildArgOperand中使用
    public int getArgId() {
        return argId;
    }

    // ============== 构造函数 ==============
    // 默认构造函数
    public Value(String name, ValueType type, Value parent) {
        this.id = applyNewId();
        this.name = name;
        this.type = type;
        this.parent = parent;
    }

    // 下面这个专门在Function.addArgsByValueType中使用，区别在于多设置isArg和argId
    public Value(String name, ValueType type, Value parent, int argId) {
        this.id = applyNewId();
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.isArg = true;
        this.argId = argId;
    }

    // ============== 相关方法 ==============
    // 向users表中添加user
    public void addUser(User user) {
        this.users.add(user);
    }

    public String toString() {
        return type + " " + name;
    }

    // =========== MIPS相关 ===========
    // buildMips本质上只有5个用法：BasicBlock, Function, GlobalVariable, Instruction, Module
    public void buildMips() {
        System.out.println("Value类 buildMips，可能出错了！");
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return id == value.id;
    }

    public int hashCode() {
        return Objects.hash(id);
    }
}
