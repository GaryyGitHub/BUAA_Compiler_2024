package utils;

import backend.instructions.MipsInstruction;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import backend.operands.MipsVReg;
import ir.values.instructions.MathInstruction;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Gary
 * @Description: MIPS相关工具类
 * @date 2024/12/5 18:15
 */
public class MipsUtils {
    /**
     * 判断一个操作数是否是寄存器
     * @param op    操作数
     * @return
     */
    public static boolean isReg(MipsOperand op) {
        return op instanceof MipsRReg || op instanceof MipsVReg;
    }

    /**
     * 在指定元素后面插入一个元素
     */
    public static void insertAfter(LinkedList<MipsInstruction> list, MipsInstruction target, MipsInstruction newInst) {
        ListIterator<MipsInstruction> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(target)) {
                iterator.add(newInst);
//                System.out.println("[insertAfter] " + target + ", " + newElement);
                break;
            }
        }
    }

    /**
     * 在指定元素前面插入一个元素
     */
    public static void insertBefore(LinkedList<MipsInstruction> list, MipsInstruction target, MipsInstruction newInst) {
        ListIterator<MipsInstruction> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(target)) {
                iterator.previous();
                iterator.add(newInst);
//                System.out.println("[insertBefore] " + target + ", " + newElement);
                break;
            }
        }
    }
}
