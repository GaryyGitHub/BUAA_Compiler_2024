package ir.values;

import backend.MipsContext;
import backend.units.MipsBlock;
import backend.units.MipsFunction;
import backend.units.MipsModule;
import ir.types.VoidType;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: llvm文件基本单位，包含全局变量和函数
 * @date 2024/11/17 19:33
 */
public class Module extends Value {
    // 单例模式
    private static final Module module = new Module();
    public static Module getInstance() {
        return module;
    }
    private Module() {
        super("Module", new VoidType(), null);
    }

    // =========== 变量定义 ===========
    // 全部函数表
    public ArrayList<Function> functions = new ArrayList<>();
    // 全部全局变量表
    public ArrayList<GlobalVariable> globalVariables = new ArrayList<>();

    // =========== 常用方法 ===========
    // 向全部函数表添加函数
    public static void addFunction(Function function) {
        module.functions.add(function);
    }

    public static void addGlobalVariable (GlobalVariable globalVariable) {
        module.globalVariables.add(globalVariable);
    }

    // 生成中间代码的字符串
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 防止没有全局变量的时候还多输出一个换行符
        if (!globalVariables.isEmpty()) {
            for (GlobalVariable globalVariable : globalVariables) {
                sb.append(globalVariable);
            }
            sb.append("\n");
        }
        if (!functions.isEmpty()) {
            for (Function function : functions) {
                sb.append(function);
            }
        }
        return sb.toString();
    }

    // =========== MIPS相关 ===========
    public void buildMips() {
        for (GlobalVariable globalVariable : globalVariables) {
            globalVariable.buildMips();
        }
        funcBlockIr2Mips();
        for (Function function : functions) {
            function.buildMips();
        }
    }

    /**
     * 把LLVM的函数和基本块映射到MIPS中
     * 1. 在MIPS构建相应对象
     * 2. 加入MipsModule中
     * 3. 把信息存储到MIPS对象中
     */
    private void funcBlockIr2Mips() {
        // 遍历所有函数
        for (Function irFunc : functions) {
            // 1.1 函数 - 构建对象
            MipsFunction mipsFunc = new MipsFunction(irFunc.getName(), irFunc.isLibFunc());
            // 1.2 函数 - 构建ir-mips映射，插入map中
            MipsContext.addFunctionMap(irFunc, mipsFunc);
            // 1.3 函数 - 加入MipsModule中
            MipsModule.addFunction(mipsFunc);

            // 2.1 基本块 - 构建对象
            ArrayList<BasicBlock> irBlocks = irFunc.getBasicBlocks();
            // 2.2 基本块 - 构建ir-mips映射，插入map中
            for (BasicBlock irBlock : irBlocks) {
                MipsContext.addBasicBlockMap(irBlock, new MipsBlock(irBlock.getName(), irBlock.getLoopDepth()));
            }

            // 2.3 基本块 - 把irBlock的前驱块的信息拷贝到对应的mipsBlock中
            for (BasicBlock irBlock : irBlocks) {
                MipsBlock mipsBlock = MipsContext.getBasicBlock(irBlock);
                for (BasicBlock irPreBlock : irBlock.getPreBlocks()) {
                    // 对于mipsblock的每个前驱块（也是mipsBlock），加入mipsBlock的前驱块列表
                    mipsBlock.addPreBlock(MipsContext.getBasicBlock(irPreBlock));
                }
            }
        }
    }

}
