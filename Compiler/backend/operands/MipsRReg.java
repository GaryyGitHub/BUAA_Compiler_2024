package backend.operands;

import backend.reg.Reg;

import java.util.Objects;

/**
 * @author Gary
 * @Description: MIPS中实际使用的寄存器
 * @date 2024/12/5 17:31
 */
public class MipsRReg extends MipsOperand {
    private Reg type;
    private boolean isAllocated;

    // ============ 构造函数 ============
    public MipsRReg(int index) {
        this.type = Reg.getRegType(index);
        this.isAllocated = false;
    }

    public MipsRReg(String name) {
        this.type = Reg.getRegType(name);
        this.isAllocated = false;
    }

    public MipsRReg(Reg type, boolean isAllocated) {
        this.type = type;
        this.isAllocated = isAllocated;
    }

    public static MipsRReg ZERO = new MipsRReg(0);
    public static MipsRReg AT = new MipsRReg("at");
    public final static MipsRReg V0 = new MipsRReg("v0");
    public final static MipsRReg SP = new MipsRReg("sp");
    public final static MipsRReg RA = new MipsRReg("ra");

    // 真实寄存器的toString
    public String toString() {
        return "$" + type.getName();
    }

    // ============ 图着色 ============
    /**
     * 若一个物理寄存器尚未被分配，则需要着色
     */
    public boolean needsColor() {
        return !isAllocated;
    }

    public boolean isPrecolored() {
        return !isAllocated;
    }

    public boolean isAllocated() {
        return isAllocated;
    }

    public Reg getType() {
        return type;
    }

    public void setAllocated(boolean isAllocated) {
        this.isAllocated = isAllocated;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsRReg reg = (MipsRReg) o;
        return type == reg.type && isAllocated == reg.isAllocated;
    }

    public int hashCode()
    {
        return Objects.hash(type.number, isAllocated);
    }
}
