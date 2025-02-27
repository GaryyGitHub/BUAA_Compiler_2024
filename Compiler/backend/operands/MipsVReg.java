package backend.operands;

import java.util.Objects;

/**
 * @author Gary
 * @Description: 虚拟寄存器，名字类似于llvm更直观，但不符合mips语法
 * @date 2024/12/5 17:48
 */
public class MipsVReg extends MipsOperand {
    // 标志虚拟寄存器编号
    private static int id = 0;

    // 虚拟寄存器名字，以v开头，代表虚拟寄存器
    private String name;

    public MipsVReg() {
        this.name = "v" + getId();
    }

    private int getId() {
        return id++;
    }

    public String toString() {
        return name;
    }

    /**
     * 但凡是虚拟寄存器，都需要着色
     */
    public boolean needsColor() {
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsVReg that = (MipsVReg) o;
        return Objects.equals(name, that.name);
    }

    public int hashCode() {
        return Objects.hash(name);
    }
}
