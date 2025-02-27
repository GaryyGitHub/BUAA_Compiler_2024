package ir;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Gary
 * @Description:
 * 生成中间代码的递归下降过程中，存储继承属性、综合属性、当前块等全局变量（上下文）的类
 * 用来进行不同下降层级之间的通信
 * @date 2024/11/17 17:08
 */
public class IrContext {
    // ============ 当前属性 ============
    // 当前所在基本块
    public static BasicBlock curBlock = null;

    // 当前所在函数，FuncDefNode第一次用到
    public static Function curFunction = null;

    // 是否正在计算无变量常数表达式(constExp)
    public static boolean isBuildingConstExp = false;

    // 是否正在加载函数实参，并且要求PointerType类型的value
    public static boolean isBuildingPointerRParam = false;

    /**
     * 用于处理多重循环的continue，栈顶的loopEndBlock就是当前层continue跳转的对象
     */
    public static Stack<BasicBlock> loopEndBlockStack = new Stack<>();
    /**
     * 用于处理多重循环的break，栈顶的endBlock就是当前层break跳转的对象
     */
    public static Stack<BasicBlock> endBlockStack = new Stack<>();

    // ============ 综合属性 ============
    // char/int类型的位数，向上传递
    public static int intBits = 0;
    // Value类的综合属性，向上传递
    public static Value synValue = null;
    // Int类型的综合属性，向上传递
    public static int synInt = -114514;
    // Value类数组的综合属性，向上传递
    public static ArrayList<Value> synValueArray = null;

    // ============ 继承属性 ============
    // int类型的继承属性，向下传递
    public static int inheritInt = -1919810;
}
