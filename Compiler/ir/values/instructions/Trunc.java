package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 整数截断，将i32截断为i8
 * <result> = trunc <ty> <value> to <ty2>
 * @date 2024/11/22 11:02
 */
public class Trunc extends Instruction {
    public Trunc(String name, BasicBlock parent, Value value) {
        super(name, new IntType(8), parent, value);
    }

    // %1 = trunc i32 %2 to i8
    public String toString() {
        // "%1 = trunc "
        return getName() + " = trunc " +
                // "i32 %2"
                IrUtils.typeAndNameStr(getOperands().get(0)) +
                // " to i8"
                " to i8";
    }

    public void buildMips() {
        Value value2Trunc = getOp(1);
        System.out.println("遇到trunc指令了！！value是：" + value2Trunc + " " + value2Trunc.getType());
        // 处理方法：将value2Trunc的低8位赋值给rd，即rd = value2Trunc & 0xff
        // andi $rd, $rs, 255
        MipsOperand rs = MipsBuilder.buildOperand(value2Trunc, false, MipsContext.curIrFunction, getParent());
        MipsOperand i255 = MipsBuilder.buildImmOperand(255, true, MipsContext.curIrFunction, getParent());
        MipsOperand rd = MipsBuilder.buildOperand(this, true, MipsContext.curIrFunction, getParent());
        MipsBuilder.buildBinary(MipsBinary.Type.AND, rd, rs, i255, getParent());
    }
}
