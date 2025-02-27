package backend.units;

import backend.instructions.MipsInstruction;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author Gary
 * @Description:
 * @date 2024/11/29 0:32
 */
public class MipsBlock {
    // ============ 成员变量 ============
    private static int nameCnt = 0;
    private String name;

    // 基本块中指令的列表
    private LinkedList<MipsInstruction> instructions = new LinkedList<>();

    // 循环深度
    private int loopDepth = 0;

    // 若基本块最后一条指令为有条件跳转指令，下面是后继块
    /**
     * 条件为false要跳转到的块
     */
    private MipsBlock falseSuccessor = null;
    /**
     * 某基本块最多能有两个后继（false或true），若有一个后继则只有true的
     */
    private MipsBlock trueSuccessor = null;

    // ============ 构造函数 ============
    public MipsBlock(String name, int loopDepth) {
        // 去掉前缀的"b"
        this.name = name.substring(1) + "_" + getNameCnt();
        this.loopDepth = loopDepth;
    }

    // ============ getter/setter ============
    public int getNameCnt() {
        return nameCnt++;
    }

    public String getName() {
        return name;
    }

    public MipsBlock getFalseSuccessor() {
        return falseSuccessor;
    }

    public void setFalseSuccessor(MipsBlock falseSuccessor) {
        this.falseSuccessor = falseSuccessor;
    }

    public MipsBlock getTrueSuccessor() {
        return trueSuccessor;
    }

    public void setTrueSuccessor(MipsBlock trueSuccessor) {
        this.trueSuccessor = trueSuccessor;
    }

    // ============== ir analyze相关 - 前驱与后继块 ==============
    private ArrayList<MipsBlock> preBlocks = new ArrayList<>();
    public void addPreBlock(MipsBlock block) {
        preBlocks.add(block);
    }

    // ============ instructions指令相关 ============
    public void addInstruction(MipsInstruction instruction) {
        instructions.add(instruction);
    }

    // 在基本块最开头插入指令
    public void addInstructionFirst(MipsInstruction instruction) {
        instructions.addFirst(instruction);
    }

    /**
     * 移除基本块中的最后一条指令
     */
    public void removeInstructionLast() {
        instructions.removeLast();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(name + ":\n");
        for (MipsInstruction instruction : instructions) {
            sb.append("\t").append(instruction);
        }
        return sb.toString();
    }

    // ============ 其他 ============
    public LinkedList<MipsInstruction> getInstructions() {
        return instructions;
    }

    // ============ 优化相关 ============
    public int getLoopDepth() {
        return loopDepth;
    }
}
