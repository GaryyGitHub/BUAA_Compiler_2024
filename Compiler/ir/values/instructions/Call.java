package ir.values.instructions;

import backend.MipsBuilder;
import backend.MipsContext;
import backend.instructions.*;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsRReg;
import backend.units.MipsBlock;
import backend.units.MipsFunction;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;
import utils.IrUtils;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 函数调用指令
 * <result> = call [ret attrs] <ty> <name>(<...args>)
 * @date 2024/11/20 13:30
 */
public class Call extends Instruction {
    private Function function;  // 函数对象

    /**
     * call指令构造函数，ValueType一定是函数返回值的ValueType
     * @param parent    所属基本块
     * @param function  operands[0] 函数对象
     * @param rArgs     operands[1,2,...] 实参列表
     */
    public Call(String name, BasicBlock parent, Function function, ArrayList<Value> rArgs) {
        super(name, function.getReturnType(), parent, new ArrayList<Value>() {{
            add(function);
            addAll(rArgs);
        }}.toArray(new Value[0]));  // 这个new Value[0]是为了告知toArray方法，该数组是Value类型的
        this.function = function;
    }

    /**
     * 获取参数列表(Value类型)
     * @return  ir中call指令传给函数的参数
     */
    public ArrayList<Value> getArgs() {
        ArrayList<Value> args = new ArrayList<>();
        // getOp(1) 就是函数对象，后面都是参数：%i14 = call i32 @func(i32 10, i32* %i13)
        for (int i = 0; i < ((Function) getOp(1)).getArgValues().size(); i++) {
            args.add(getOp(i + 2));
        }
        return args;
    }

    // 有两种形式：
    // 1. 有返回值：%3 = call i32 @bar(i32 %1, i32 %2)
    // 2. 无返回值：call void @foo(i32 %1)
    public String toString() {
        // ops是指令的操作数列表
        ArrayList<Value> ops = new ArrayList<>(getOperands());
        // 指令的第一个参数为函数对象，把它取出来
        Function func = (Function) ops.get(0);
        StringBuilder sb = new StringBuilder();
        // 若是有返回值：还需要在前面加上形如 "%3 = " 的语句
        if (!(function.getReturnType() instanceof VoidType)) {
            sb.append(getName()).append(" = ");
        }
        // 接下来是1和2共同的 "call i32 @bar"
        sb.append("call ").append(func.getReturnType()).append(" ").append(func.getName()).append("(");
        // 有参数的函数，需要把参数列表也打印出来: "(i32 %1, i32 %2)"
        if (ops.size() >= 1) {
            ops.remove(0);  // 移除函数对象
            IrUtils.appendSBParamList(sb, ops);
        }
        sb.append(")");
        return sb.toString();
    }

    public void buildMips() {
        // 1. 先获取到当前基本块和当前被调用的函数
        MipsBlock mipsBlock = MipsContext.getBasicBlock(getParent());
        MipsFunction mipsFunction = MipsContext.getFunction(function);
        MipsInstruction call;
        // 2. 构建call指令（jal）
        // FIXME: libFunc不能使用宏调用，需要额外处理！
        if (mipsFunction.isLibFunc()) {
            call = new MipsLibFunc(mipsFunction.getName());
            // 系统调用必然会改变v0.需要把v0加入def
            call.addDefReg(MipsRReg.V0);
        } else {
            call = new MipsCall(mipsFunction);
        }
        // 3. 遍历所有irValue参数，传参
        int argCnt = getArgs().size();
        for (int i = 0; i < argCnt; i++) {
            Value irArg = getArgs().get(i);
            // 3.1 前四个参数：直接从src中move到$a0~$a3中: 立即数li，全局变量la，寄存器move
            if (i < 4) {
                MipsOperand src = MipsBuilder.buildOperand(irArg, true, MipsContext.curIrFunction, getParent());
                MipsMove move = MipsBuilder.buildMove(new MipsRReg("a" + i), src, getParent());
                // 向useRegs中添加$a0~$a3, 这里的寄存器要从move指令中获取
                call.addUseReg(move.getDst());
            }
            // 3.2 后面的参数，先存进寄存器，后store到内存：若是立即数会先li到新寄存器中
            else {
                // 3.2.1 先存进寄存器
                MipsOperand src = MipsBuilder.buildOperand(irArg, false, MipsContext.curIrFunction, getParent());
                // 3.2.2 存到 sp - 4 * offset 的位置: sw v14, -4($sp)
                MipsImm opOffset = new MipsImm(-4 * (argCnt - i));
                MipsBuilder.buildStore(src, MipsRReg.SP, opOffset, getParent());
            }
        }
        // 4. 栈的生长: subiu $sp, $sp, 4
        if (argCnt > 4) {
            // 栈的生长量为参数个数 - 4, 要执行减法指令
            MipsOperand offsetImm = MipsBuilder.buildImmOperand(4 * (argCnt - 4), true, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.SUBU, MipsRReg.SP, MipsRReg.SP, offsetImm, getParent());
        }
        // 5. 参数传递完毕，调用jal指令
        mipsBlock.addInstruction(call);
        // 6. 栈的恢复: addiu $sp, $sp, 4
        if (argCnt > 4) {
            // 栈的恢复量为参数个数 - 4, 要执行加法指令
            MipsOperand offsetImm = MipsBuilder.buildImmOperand(4 * (argCnt - 4), true, MipsContext.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.Type.ADDU, MipsRReg.SP, MipsRReg.SP, offsetImm, getParent());
        }
        // 7. 将$a0~$a3、非库函数的ra返回地址加入到defRegs中
        for (int i = 0; i < 4; i++) {
            call.addDefReg(new MipsRReg("a" + i));
        }
        if (!mipsFunction.isLibFunc()) {
            call.addDefReg(MipsRReg.RA);
        }
        // 8. 处理返回值
        ValueType returnType = function.getReturnType();
        // 调用者需要保存v0作为defRegs
        call.addDefReg(MipsRReg.V0);
        // 若有返回值，则需要move v0 到 dst 中
        if (!(returnType instanceof VoidType)) {
            MipsOperand dst = MipsBuilder.buildOperand(this, false, MipsContext.curIrFunction, getParent());
            // move v8, $v0 其中v8是dst寄存器, 是新分配的寄存器
            MipsBuilder.buildMove(dst, MipsRReg.V0, getParent());
        }
    }
}
