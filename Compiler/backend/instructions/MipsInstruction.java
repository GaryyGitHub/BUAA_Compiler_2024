package backend.instructions;

import backend.operands.MipsOperand;
import utils.MipsUtils;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: Mips指令类
 * @date 2024/11/29 14:55
 */
public class MipsInstruction {
    // ============ 成员变量 ============
    // 目的操作数，一条指令最多一个
    private MipsOperand dst = null;

    // 源操作数，一条指令最多三个，初始化的时候插入三个null
    protected ArrayList<MipsOperand> src = new ArrayList<>() {{
        add(null); add(null); add(null);
    }};

    // 用于活跃变量分析，记录该指令当中左值（def）寄存器
    private ArrayList<MipsOperand> defRegs = new ArrayList<>();

    // 用于活跃变量分析，记录该指令当中右值（use）寄存器
    private ArrayList<MipsOperand> useRegs = new ArrayList<>();

    // ============ setter / getter ============
    // 设置dst操作数
    public void setDst(MipsOperand dst) {
        if (dst != null) {
            // 若dst是寄存器，则把老的dst从defRegs中移除
            if (MipsUtils.isReg(this.dst)) {
                defRegs.remove(this.dst);
            }
            // 若dst是寄存器，则向defRegs中加入新的dst
            addDefReg(dst);
        }
        this.dst = dst;
    }

    public MipsOperand getDst() {
        return dst;
    }

    // 设置src操作数，index从1开始
    public void setSrc(int index, MipsOperand src) {
        if (src != null) {
            // 若src是寄存器，则把老的src从useRegs中移除
            if (MipsUtils.isReg(this.src.get(index - 1))) {
                useRegs.remove(this.src.get(index - 1));
            }
            // 若src是寄存器，则向useRegs中加入新的src
            addUseReg(src);
        }
        // 对应位置的src操作数设置为src
        this.src.set(index - 1, src);
    }

    // 注意编号从1开始！
    public MipsOperand getSrc(int index) {
        return src.get(index - 1);
    }

    public ArrayList<MipsOperand> getDefRegs() {
        return defRegs;
    }

    public ArrayList<MipsOperand> getUseRegs() {
        return useRegs;
    }

    // ============ 成员函数 ============
    // 向defRegs中加入薪的dst寄存器
    public void addDefReg(MipsOperand reg) {
        if (MipsUtils.isReg(reg)) {
            defRegs.add(reg);
        }
    }

    // 向useRegs中加入薪的src寄存器
    public void addUseReg(MipsOperand reg) {
        if (MipsUtils.isReg(reg)) {
            useRegs.add(reg);
        }
    }
    // ============ 构造函数 ============
    /**
     * 双操作数指令的构造函数
     * 一个dst，一个src
     */
    public MipsInstruction(MipsOperand dst, MipsOperand src) {
        setDst(dst);
        // 设置src操作数，index从1开始
        setSrc(1, src);
    }

    /**
     * 三操作数指令的构造函数
     * 一个dst，2个src
     */
    public MipsInstruction(MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        setDst(dst);
        setSrc(1, src1);
        setSrc(2, src2);
    }

    /**
     * 为特殊的store指令设置：没有dst（def）
     * 3个操作数均为use
     */
    public MipsInstruction(MipsOperand src1, MipsOperand src2, MipsOperand src3, boolean isStore) {
        setSrc(1, src1);
        setSrc(2, src2);
        setSrc(3, src3);
    }

    // 为了不需要super的子类方便的构造函数
    public MipsInstruction() {}

    /**
     * 替换指令中所有的指定寄存器
     */
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if (dst != null && dst.equals(oldReg)) {
            setDst(newReg);
        }
        for (int i = 0; i < src.size(); i++) {
            MipsOperand srcReg = src.get(i);
            if (srcReg != null && srcReg.equals(oldReg)) {
                setSrc(i + 1, newReg);
            }
        }
    }
}
