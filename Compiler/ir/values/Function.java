package ir.values;

import backend.MipsContext;
import backend.units.MipsBlock;
import backend.units.MipsFunction;
import ir.IrSymTable;
import ir.types.ValueType;
import utils.IrUtils;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 函数
 * @date 2024/11/17 19:04
 */
public class Function extends Value {
    // 以下5个属性用在LLVM IR开头的define指令中
    public static Function getint = null;   // declare i32 @getint()
    public static Function getchar = null;   // declare i32 @getchar()
    public static Function putint = null;   // declare void @putint(i32)
    public static Function putch = null;   // declare void @putch(i32)
    public static Function putstr = null;   // declare void @putstr(i8*)

    // ============ 基本属性 ============
    // 函数返回类型
    private ValueType returnType;

    // 是否是链接来的库函数
    private boolean isLibFunc = false;
    public boolean isLibFunc() { return isLibFunc; }

    // 形参列表
    private final ArrayList<Value> argValues = new ArrayList<>();

    // 下属的基本块列表
    private ArrayList<BasicBlock> basicBlocks = new ArrayList<>();

    // 下属符号表
    private IrSymTable symbolTable;

    // ============ getter/setter ============
    // 获取函数头部的第一个基本块，也即入口快
    public BasicBlock getHeadBlock() {
        return basicBlocks.get(0);
    }

    // 给函数添加下属形参列表
    public void setSymbolTable(IrSymTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    // 获取函数的形参表
    public ArrayList<Value> getArgValues() {
        return argValues;
    }

    public ValueType getReturnType() {
        return returnType;
    }

    // 获取函数下属的所有基本块
    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    // ============ 常用方法 ============
    // 给函数添加新参数。会把新参数加入符号表，构建对应Value并加入参数表
    public void addArgsByValueType(ValueType argType, int argNum) {
        // name就是%arg0, %arg1, %arg2, %arg3..., argValues.size()随着参数的加入而递增
        Value arg = new Value("%arg"+argValues.size(), argType, this, argNum);
        argValues.add(arg);
    }

    // 向基本块列表尾部添加基本块
    public void addBlock(BasicBlock block) {
        basicBlocks.add(block);
    }

    // ============ 构造函数 ============
    // IrBuilder.buildFunction中使用
    /**
     * @param name       函数名
     * @param returnType 函数返回类型
     * @param argTypes   函数参数的类型
     */
    public Function(String name, ValueType returnType, ArrayList<ValueType> argTypes, boolean isLibFunc) {
        super("@"+name, returnType, Module.getInstance());
        this.returnType = returnType;
        this.isLibFunc = isLibFunc;
        // 建立参数列表
        for (int i = 0; i < argTypes.size(); i++) {
            addArgsByValueType(argTypes.get(i), i);
        }
    }

    /**
     * 1. 普通函数：
     * define dso_local i32 @func(i32 %0, i32* %1) {
     *     %3 = alloca i32*
     *     ret i32 %4
     * }
     * 2. 库函数：
     * declare void @putint(i32)
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 1. 函数声明部分
        if (!isLibFunc) {
            // 1.1 普通函数：
            sb.append("define dso_local ");
        } else {
            // 1.2 库函数：
            sb.append("declare ");
        }
        // "i32 @func" "void @putint"
        sb.append(getReturnType()).append(" ").append(getName());
        // 2. 参数列表以及函数主体
        if (!isLibFunc) {
            // 2.1 普通函数：
            // 参数列表
            sb.append("(");
            IrUtils.appendSBParamList(sb, argValues);
            sb.append(") {\n");
            // 函数主体
            for (BasicBlock block : basicBlocks) {
                sb.append(block);
            }
            sb.append("}");
        } else {
            // 2.2 库函数：
            sb.append("(");
            for (Value arg : argValues) {
                sb.append(arg.getType()).append(", ");
            }
            IrUtils.cutSBTailComma(sb);
            sb.append(")");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========== MIPS相关 ===========
    public void buildMips() {
        // 只有非库函数才需要解析
        if (!isLibFunc) {
            MipsContext.curIrFunction = this;
            for (BasicBlock block : basicBlocks) {
                block.buildMips();
            }
            MipsFunction mipsFunction = MipsContext.getFunction(this);
            // 函数头部的第一个基本块
            MipsBlock firstMipsBlock = MipsContext.getBasicBlock(getHeadBlock());

            // 无优化
            mipsFunction.blockTraverse(firstMipsBlock);
        }
    }
}
