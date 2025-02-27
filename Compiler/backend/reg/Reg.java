package backend.reg;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Gary
 * @Description: MIPS寄存器类型，共32个
 * @date 2024/11/29 17:51
 */
public enum Reg {
    ZERO(0, "zero"),
    AT(1, "at"),

    // $v0 ~ $v1  作为函数返回值，一般返回值只使用$v0，当返回值超过32位时需要同时使用$v1
    V0(2, "v0"),
    V1(3, "v1"),

    // $a0 ~ $a3  用于传递参数的4个寄存器
    A0(4, "a0"),
    A1(5, "a1"),
    A2(6, "a2"),
    A3(7, "a3"),

    // $t0 ~ $t9 (8~15,24,25)  临时寄存器，用于基本块内的变量，发生函数调用时不必保存。
    T0(8, "t0"),
    T1(9, "t1"),
    T2(10, "t2"),
    T3(11, "t3"),
    T4(12, "t4"),
    T5(13, "t5"),
    T6(14, "t6"),
    T7(15, "t7"),

    // $s0 ~ $s7 (16~23)  全局寄存器，这些寄存器用于跨基本块的变量，往往需要在发生函数调用时进行保存。
    S0(16, "s0"),
    S1(17, "s1"),
    S2(18, "s2"),
    S3(19, "s3"),
    S4(20, "s4"),
    S5(21, "s5"),
    S6(22, "s6"),
    S7(23, "s7"),
    T8(24, "t8"),
    T9(25, "t9"),
    K0(26, "k0"),
    K1(27, "k1"),
    // $gp: 静态数据的全局指针寄存器
    GP(28, "gp"),
    // $sp: 栈帧寄存器，保存栈顶（低地址）
    SP(29, "sp"),
    // $fp: 栈帧寄存器，保存栈顶（高地址）
    FP(30, "fp"),
    // $ra: 返回地址，jal 指令会自动将下一条指令的地址保存在 $ra 中，从而函数调用可以正确的返回
    RA(31, "ra");

    public final int number;
    public final String name;

    Reg(int number, String name) {
        this.number = number;
        this.name = name;
    }

    /**
     * 寄存器类型名称与类型的映射
     */
    public static final HashMap<String, Reg> nameMap = new HashMap<>();

    /**
     * RegBuilder中能够被分配出去的寄存器类型集合
     */
    public static final HashSet<Reg> regs4Alloca = new HashSet<>();

    /**
     * 进行函数调用时，调用者需要保存的（现场）寄存器
     */
    public static final HashSet<Reg> regs2Save = new HashSet<>();
    static {
        for(Reg reg : values()) {
            nameMap.put(reg.name, reg);
            regs2Save.add(reg);
        }
        // 要去除下列寄存器
        regs2Save.remove(ZERO);
        regs2Save.remove(AT);
        regs2Save.remove(A0);
        regs2Save.remove(A1);
        regs2Save.remove(A2);
        regs2Save.remove(A3);
        regs2Save.remove(SP);
        regs2Save.remove(V0);
        // 只有t和s寄存器可被分配
        regs4Alloca.add(T0);
        regs4Alloca.add(T1);
        regs4Alloca.add(T2);
        regs4Alloca.add(T3);
        regs4Alloca.add(T4);
        regs4Alloca.add(T5);
        regs4Alloca.add(T6);
        regs4Alloca.add(T7);
        regs4Alloca.add(T8);
        regs4Alloca.add(T9);
        regs4Alloca.add(S0);
        regs4Alloca.add(S1);
        regs4Alloca.add(S2);
        regs4Alloca.add(S3);
        regs4Alloca.add(S4);
        regs4Alloca.add(S5);
        regs4Alloca.add(S6);
        regs4Alloca.add(S7);
    }

    public String getName() {
        return name;
    }

    // 由索引获取寄存器类型
    public static Reg getRegType(int index) {
        if (index < 0 || index >= values().length) {
            return values()[0];
        }
        return values()[index];
    }

    // 由寄存器名获取寄存器类型
    public static Reg getRegType(String name) {
        if (nameMap.containsKey(name)) {
            return nameMap.get(name);
        }
        return values()[0];
    }

    // 和MipsRReg的toString方法一样
    public String toString() {
        return "$" + name;
    }
}
