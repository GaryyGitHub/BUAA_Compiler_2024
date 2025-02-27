package ir.values;

import backend.units.MipsGlobalVariable;
import backend.units.MipsModule;
import ir.types.PointerType;
import ir.values.constants.*;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 全局变量
 * @date 2024/11/17 21:03
 */
public class GlobalVariable extends User {
    private boolean isConst;
    private Constant initValue = null;

    public Constant getInitValue() {
        return initValue;
    }

    /**
     * 对于 常量（const）声明，一定要初始化
     * 全局变量本身以指针形式存在
     * 全局变量在输出为LLVM时，以@开头
     */
    public GlobalVariable(String name, boolean isConst, Constant initValue) {
        super("@" + name, // 输出的名字形如 @name
                new PointerType(initValue.getType()),   // 全局变量本身以指针形式存在
                Module.getInstance(), // 当前value的父value就是Module根节点
                new ArrayList<>(){{ add(initValue); }});
        this.isConst = isConst;
        this.initValue = initValue;
    }

    // @a = dso_local global [3 x i32] [i32 1, i32 2, i32 3]
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = dso_local ");
        // 常量或变量
        if (isConst) {
            sb.append("constant ");
        } else {
            sb.append("global ");
        }
        // getOperands().get(0)就是初始化的值
        sb.append(getOperands().get(0).getType());
        // "[i32 1, i32 2, i32 3]"
        sb.append(" ").append(getOperands().get(0)).append("\n");
        return sb.toString();
    }

    // =========== MIPS全局变量 ===========
    public void buildMips() {
        MipsGlobalVariable mipsGlobalVariable = null;
        // 没有初始值，不该出现！
        if (initValue == null) {
            System.out.println("Gary: MIPS全局变量GlobalVariable " + getName() + " initValue == null");
        }
        // 未初始化的int/char数组
        else if (initValue instanceof ZeroInitializer) {
            mipsGlobalVariable = new MipsGlobalVariable(getName(), initValue.getType().getSize(true));
        }
        // 常量字符串
        else if (initValue instanceof ConstString) {
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ((ConstString) initValue).getContent());
        }
        // int/char变量
        else if (initValue instanceof ConstInt) {
            mipsGlobalVariable = new MipsGlobalVariable(getName(), new ArrayList<>(){{
                add( ((ConstInt) initValue) );
            }});
        }
        // 已初始化的int/char数组
        else if (initValue instanceof ConstArray) {
            ArrayList<ConstInt> constInts = new ArrayList<>();
            for (Constant c : ((ConstArray) initValue).getElements()) {
                constInts.add( ((ConstInt) c) );
            }
            mipsGlobalVariable = new MipsGlobalVariable(getName(), constInts);
        }
        MipsModule.addGlobalVariable(mipsGlobalVariable);
    }
}
