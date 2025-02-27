package ir.values;

import ir.analyze.Loop;
import ir.types.LabelType;
import ir.values.instructions.Instruction;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author Gary
 * @Description: 基本块
 * @date 2024/11/18 17:33
 */
public class BasicBlock extends Value {
    // 指令序列
    private LinkedList<Instruction> instructions = new LinkedList<>();

    /**
     * @param parent 所属Function
     */
    public BasicBlock (String name, Value parent) {
        super ("%b" + name, new LabelType(), parent);
    }

    // ============== getter/setter ==============
    // 返回这个基本块所属的父亲函数
    public Function getParentFunction() {
        return (Function) super.getParent();
    }

    // ============== 常用方法 ==============
    /**
     * 在指令序列的末尾添加一条指令
     */
    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    /**
     * 在指令序列的开头添加一条指令，多用于alloca指令
     */
    public void addInstructionAtHead(Instruction instruction) {
        instructions.addFirst(instruction);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // "b0:\n"
        sb.append(getName().substring(1)).append(":\n");
        // 从此开始构造基本快中的每条指令，每条指令前面有个制表符缩进
        for (Instruction inst : instructions) {
            sb.append("\t").append(inst).append("\n");
        }
        return sb.toString();
    }

    // ============== ir analyze相关 - 前驱与后继块 用于控制流图分析 ==============
    private HashSet<BasicBlock> preBlocks = new HashSet<>();
    private HashSet<BasicBlock> succBlocks = new HashSet<>();
    public HashSet<BasicBlock> getPreBlocks() {
        return preBlocks;
    }
    public HashSet<BasicBlock> getSuccBlocks() {
        return succBlocks;
    }
    public void addPreBlock(BasicBlock block) {
        preBlocks.add(block);
    }
    public void addSuccBlock(BasicBlock block) {
        succBlocks.add(block);
    }

    // ============== 循环信息 ==============
    // 当前基本块所在的循环
    private Loop loop = null;

    public int getLoopDepth() {
        if (loop == null) {
            return 0;
        }
        return loop.getLoopDepth();
    }

    // =========== MIPS相关 ===========
    public void buildMips() {
        for (Instruction inst : instructions) {
            // 基本块中每条llvm指令都要转化成mips指令，在这里进行！
            inst.buildMips();
        }
    }
}
