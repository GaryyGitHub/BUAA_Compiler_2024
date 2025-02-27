package backend;

import backend.operands.MipsOperand;
import backend.units.MipsBlock;
import backend.units.MipsFunction;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.HashMap;

/**
 * @author Gary
 * @Description: MIPS上下文通信类，包含ir和mips的映射关系
 * @date 2024/11/29 0:11
 */
public class MipsContext {
    // ============ 成员变量 ============
    // 当前正在解析的irFunction，只在Function的buildMips时进行赋值
    public static Function curIrFunction = null;

    // ir函数与mips函数的映射
    private static HashMap<Function, MipsFunction> functionMap = new HashMap<>();

    // ir基本块与mips基本块的映射
    private static HashMap<BasicBlock, MipsBlock> basicBlockMap = new HashMap<>();

    // ir操作数与mips操作数的映射
    private static HashMap<Value, MipsOperand> opMap = new HashMap<>();

    // ============ 成员方法 ============
    /**
     * 添加ir函数与mips函数的映射
     */
    public static void addFunctionMap(Function irFunction, MipsFunction mipsFunction) {
        functionMap.put(irFunction, mipsFunction);
    }

    /**
     * 添加ir基本块与mips基本块的映射
     */
    public static void addBasicBlockMap(BasicBlock irBasicBlock, MipsBlock mipsBlock) {
        basicBlockMap.put(irBasicBlock, mipsBlock);
    }

    /**
     * 添加irValue与mipsOperand的映射
     * 记录指令的目的寄存器与arg参数，不记录imm和label
     */
    public static void addOperandMap(Value irValue, MipsOperand mipsOperand) {
        opMap.put(irValue, mipsOperand);
    }

    /**
     * 获取ir函数对应的mips函数
     * @param irFunction    ir函数
     * @return            mips函数
     */
    public static MipsFunction getFunction(Function irFunction) {
        return functionMap.get(irFunction);
    }

    /**
     * 获取ir基本块对应的mips基本块
     * @param irBasicBlock    ir基本块
     * @return                mips基本块
     */
    public static MipsBlock getBasicBlock(BasicBlock irBasicBlock) {
        return basicBlockMap.get(irBasicBlock);
    }

    public static MipsOperand getOperand(Value irValue) {
        return opMap.get(irValue);
    }
}
