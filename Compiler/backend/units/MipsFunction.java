package backend.units;

import backend.MipsBuilder;
import backend.instructions.MipsInstruction;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import backend.operands.MipsVReg;
import backend.reg.Reg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * @author Gary
 * @Description:
 * @date 2024/11/28 22:03
 */
public class MipsFunction {
    // ============ 成员变量 ============
    private String name;
    private boolean isLibFunc;
    /**
     * 函数在栈上已经分配出的空间：包含alloca和spill(溢出)
     */
    private int allocaSize = 0;
    /**
     * 函数在栈上应分配的总空间
     */
    private int totalStackSize;
    /**
     * 函数的基本块列表
     */
    private ArrayList<MipsBlock> mipsBlocks = new ArrayList<>();
    /**
     * 存储函数调用前需要保存的寄存器集合
     */
    private TreeSet<Reg> regs2Save = new TreeSet<>();
    /**
     * 虚拟寄存器集合
     */
    private HashSet<MipsVReg> usedVRegs = new HashSet<>();

    // 已经被遍历（序列化）过的block
    private HashSet<MipsBlock> traversedBlocks = new HashSet<>();

    /**
     * 函数参数（id>=4的部分）在栈上的偏移量集合
     */
    private HashSet<MipsImm> argOffsets = new HashSet<>();

    // ============ 构造函数 ============
    public MipsFunction(String name, boolean isLibFunc) {
        // 去掉前缀"@"
        this.name = name.substring(1);
        this.isLibFunc = isLibFunc;
        this.allocaSize = 0;
    }

    // ============ 成员方法 ============
    public String getName() {
        return name;
    }

    public boolean isLibFunc() {
        return isLibFunc;
    }

    /**
     * 获取当前函数在栈上已经分配的空间
     */
    public int getAllocaSize() {
        return allocaSize;
    }

    public int getTotalStackSize() {
        return totalStackSize;
    }

    public TreeSet<Reg> getRegs2Save() {
        return regs2Save;
    }

    /**
     * 在函数栈上分配指定大小的空间
     */
    public void addAllocaSize(int size) {
        allocaSize += size;
        System.out.println("allocaSizeNow: "+allocaSize);
    }

    /**
     * 向函数中添加一个虚拟寄存器
     */
    public void addUsedVReg(MipsVReg vReg) {
        usedVRegs.add(vReg);
    }

    public HashSet<MipsVReg> getUsedVRegs() {
        return usedVRegs;
    }

    // 向函数参数偏移量集合中添加一个偏移量(仅当参数>4时才会调用)
    public void addArgOffset(MipsImm offset) {
        argOffsets.add(offset);
    }

    /**
     * 序列化基本块：DFS遍历函数中所有**后继**基本块，构建跳转关系。
     * 后继最多有两个
     * @param curBlock 当前所处块
     */
    public void blockTraverse(MipsBlock curBlock) {
        // 把当前块加入已遍历集合
        traversedBlocks.add(curBlock);
        // 将当前块插入基本块列表
        mipsBlocks.add(curBlock);

        // 1. 没有后继，则终止
        if (curBlock.getTrueSuccessor() == null && curBlock.getFalseSuccessor() == null) {
            return;
        }
        // 2. 只有一个后继，只可能是true后继，考虑和当前块合并
        // 不一定是跳转的后继，有可能就是顺序执行的后继，和理论课的基本块划分一样
        if (curBlock.getFalseSuccessor() == null) {
            MipsBlock successor = curBlock.getTrueSuccessor();
            // 若后继块已经序列化，说明后继块的代码在当前块的前面，不需要进行合并
            // 若后继块还没有序列化，则需要进行合并，所谓合并就是删掉块末尾的j指令
            if (!traversedBlocks.contains(successor)) {
                curBlock.removeInstructionLast();
                // 递归序列化后继块
                blockTraverse(successor);
            }
        }
        // 3. 有两个后继，一般是if-else语句的两个分支，考虑合并
        else {
            MipsBlock trueSuccessor = curBlock.getTrueSuccessor();
            MipsBlock falseSuccessor = curBlock.getFalseSuccessor();

            // trueBlock是间接跳转块，falseBlock（else语句）是紧跟随的块
            // 若已经序列化，还需要增加一条branch指令，跳转到已经序列化的后继块
            if (traversedBlocks.contains(falseSuccessor)) {
                MipsBuilder.buildBranch(falseSuccessor, curBlock);
            }
            // 对falseBlock和trueBlock进行序列化
            if (!traversedBlocks.contains(falseSuccessor)) {
                blockTraverse(falseSuccessor);
            }
            if (!traversedBlocks.contains(trueSuccessor)) {
                blockTraverse(trueSuccessor);
            }
        }
    }

    /**
     * 打印函数相关代码
     */
    public String toString() {
        // 不鸟库函数
        if (isLibFunc) return "";
        // 1. 打印函数头
        StringBuilder sb = new StringBuilder(name + ":\n");
        // 2. 如果不是main，需要保存被调用者寄存器至栈中，和MipsRet的恢复寄存器对应
        if (!name.equals("main")) {
            int stackOffset = -4;
            for (Reg reg : regs2Save) {
                sb.append("\tsw\t").append(reg).append(",\t").append(stackOffset).append("($sp)\n");
                stackOffset -= 4;
            }
        }
        // 3. 进入函数体前，要移动栈指针$sp，和MipsRet把$sp恢复对应
        if (totalStackSize > 0) {
            sb.append("\tadd\t$sp,\t$sp,\t").append(-totalStackSize).append("\n");
        }
        // 4. 打印函数中的各个基本块
        for (MipsBlock block : mipsBlocks) {
            sb.append(block);
        }
        return sb.toString();
    }

    // ============ 图着色算法要用到的新函数 =============
    public ArrayList<MipsBlock> getMipsBlocks() {
        return mipsBlocks;
    }

    /**
     * 栈上的空间从上到下依次为：
     * 1.调用者保存的寄存器
     * 2.其他alloca
     * 3.参数alloca
     */
    public void rebuildStack() {
        // 遍历下属所有语句，记录所有用过的寄存器，作为函数调用前要保存的现场
        for (MipsBlock block : mipsBlocks) {
            for (MipsInstruction instr : block.getInstructions()) {
                for (MipsOperand defReg : instr.getDefRegs()) {
                    if (defReg instanceof MipsRReg) {
                        Reg reg = ((MipsRReg) defReg).getType();
                        if (Reg.regs2Save.contains(reg)) {
                            regs2Save.add(reg);
                        }
                    } else {
                        System.out.println("函数" + name + "中使用了虚拟寄存器：" + defReg);
                    }
                }
            }
        }
        // 需要分配的用于保存现场的空间
        int stackRegSize = 4 * regs2Save.size();
        // 总的空间大小：alloca空间 + 保存现场的空间
        totalStackSize = stackRegSize + allocaSize;
        // 更新先前记录的 保存在栈上的参数 的位移
        for (MipsImm argOffset : argOffsets) {
            int newOffset = argOffset.getValue() + totalStackSize;
            argOffset.setValue(newOffset);
        }
    }
}
