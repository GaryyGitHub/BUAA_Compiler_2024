package backend.operands;

/**
 * @author Gary
 * @Description:
 * @date 2024/12/5 16:11
 */
public abstract class MipsOperand {
    /**
     * 若一个物理寄存器尚未被分配，则需要着色
     * @return 是否需要着色
     */
    public boolean needsColor() {
        return false;
    }

    /**
     * 虚拟寄存器永远为false
     * 物理寄存器：尚未被分配就是precolored
     * 因为预着色的寄存器都是在 irParse 阶段分配的，此时的 isAllocated == false，所以是预着色的
     * isAllocated == true 的物理寄存器只会发生在着色阶段
     * @return
     */
    public boolean isPrecolored() {
        return false;
    }

    public boolean isAllocated() {
        return false;
    }
}
