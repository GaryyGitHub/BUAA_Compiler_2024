package ir.values.instructions;

import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.User;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 指令类。本质上来说，Instruction就是各种具体指令的返回值的Value对象，因此只需要记录操作数
 * @date 2024/11/18 17:29
 */
public class Instruction extends User {
    /**
     * @param name  指令Value的名称（虚拟寄存器名）
     * @param type  指令Value的类型
     * @param parent    parent一定是BasicBlock
     * @param operands  所属操作数列表，这是可以为空的，传0或多个参数，省的多写一个构造函数
     */
    public Instruction(String name, ValueType type, BasicBlock parent, Value... operands) {
        // %i 代表LLVM中的临时寄存器，它以后可能也会成为别人的操作数
        super("%i" + name, type, parent, new ArrayList<>(){{
            addAll(List.of(operands));
        }});
    }

    /**
     * 给MIPS生成使用，获取父基本块
     * @return 父基本块
     */
    public BasicBlock getParent() {
        return ((BasicBlock) super.getParent());
    }
}
