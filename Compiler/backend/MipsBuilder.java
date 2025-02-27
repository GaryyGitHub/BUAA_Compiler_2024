package backend;

import backend.instructions.*;
import backend.operands.*;
import backend.reg.RegBuilder;
import backend.units.MipsBlock;
import backend.units.MipsFunction;
import backend.units.MipsModule;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstInt;
import utils.IOUtils;
import utils.MathUtils;

/**
 * @author Gary
 * @Description: MIPS汇编代码生成的控制类，Compiler从此调用
 * 还是工厂类，但凡调用了一次build*方法，最后就会多一条mips语句
 * @date 2024/11/28 20:25
 */
public class MipsBuilder {
    private Module irModule;

    public MipsBuilder(Module irModule) {
        this.irModule = irModule;
    }

    // 生成MIPS代码的主要方法
    public void generate() {
        // 这一步生成的MIPS的寄存器还是虚拟寄存器，没有考虑寄存器分配
        irModule.buildMips();
        // 下面进行寄存器分配
        RegBuilder regBuilder = new RegBuilder();
        regBuilder.buildRegs();
    }

    // 输出MIPS代码
    public void printResult() {
        System.out.println(">>>>> 开始输出MIPS >>>>>");
        IOUtils.write(MipsModule.getInstance().toString());
        System.out.println(">>>>> 输出MIPS OK >>>>>");
    }

    // ============ 各类指令的构造方法 ============
    /**
     * 无条件跳转分支指令
     * j target
     * @param target    跳转目标
     * @param mipsBlock 当前MipsBlock
     * @return          MipsBranch指令对象
     */
    public static MipsBranch buildBranch(MipsBlock target, MipsBlock mipsBlock) {
        MipsBranch br = new MipsBranch(target);
        mipsBlock.addInstruction(br);
        return br;
    }

    /**
     * 有条件跳转分支指令
     * blt v0, v2, b1_1
     * @param condType  条件判断类型
     * @param op1   条件判断的左操作数
     * @param op2   条件判断的右操作数
     */
    public static MipsBranch buildBranch(MipsCondType condType, MipsOperand op1, MipsOperand op2, MipsBlock target, MipsBlock mipsBlock) {
        MipsBranch br = new MipsBranch(condType, op1, op2, target);
        mipsBlock.addInstruction(br);
        return br;
    }

    /**
     * 构建move指令
     * @param dst      目的操作数
     * @param src      源操作数
     * @param irBlock   指令所处的irBlock
     * @return        MipsMove指令对象
     */
    public static MipsMove buildMove(MipsOperand dst, MipsOperand src, BasicBlock irBlock) {
        MipsMove move = new MipsMove(dst, src);
        MipsContext.getBasicBlock(irBlock).addInstruction(move);
        return move;
    }

    /**
     * 存储指令sw，把word（4字节）从寄存器存到内存dstBase+offset处
     * @param src       要存储的寄存器
     * @param dstBase   目的内存基址
     * @param offset    目的内存相对于基址的偏移量
     */
    public static MipsStore buildStore(MipsOperand src, MipsOperand dstBase, MipsOperand offset, BasicBlock irBlock) {
        MipsStore store = new MipsStore(src, dstBase, offset);
        MipsContext.getBasicBlock(irBlock).addInstruction(store);
        return store;
    }

    /**
     * 加载指令lw，把word（4字节）从内存srcBase+offset处取到寄存器dst
     * @param dst       要加载到的寄存器
     * @param srcBase      源内存基址
     * @param offset    源内存相对于基址的偏移量
     */
    public static MipsLoad buildLoad(MipsOperand dst, MipsOperand srcBase, MipsOperand offset, BasicBlock irBlock){
        MipsLoad load = new MipsLoad(dst, srcBase, offset);
        MipsContext.getBasicBlock(irBlock).addInstruction(load);
        return load;
    }

    /**
     * 构建双操作数的指令
     */
    public static MipsBinary buildBinary(MipsBinary.Type type, MipsOperand dst, MipsOperand src1, MipsOperand src2, BasicBlock irBlock) {
        MipsBinary binary = new MipsBinary(type, dst, src1, src2);
        MipsContext.getBasicBlock(irBlock).addInstruction(binary);
        return binary;
    }

    /**
     * 构建比较指令，在Icmp中用到
     */
    public static MipsCompare buildCompare(MipsCondType type, MipsOperand dst, MipsOperand src1, MipsOperand src2, BasicBlock irBlock) {
        MipsCompare cmp = new MipsCompare(type, dst, src1, src2);
        MipsContext.getBasicBlock(irBlock).addInstruction(cmp);
        return cmp;
    }

    public static MipsRet buildRet(Function irFunction, BasicBlock irBlock) {
        MipsRet ret = new MipsRet(MipsContext.getFunction(irFunction));
        MipsContext.getBasicBlock(irBlock).addInstruction(ret);
        return ret;
    }

    // ============ 操作数Operands的构造方法 ============
    /**
     * MipsOperand的构造方法，将irValue对象转换为MipsOperand对象
     * @param irValue       irValue对象
     * @param isImm         是否需要立即数op，就是是否允许在mips代码中以数字形式出现
     * @param irFunction    所处的irFunction
     * @param irBlock       所处的irBlock
     * @return              最后生成的MipsOperand对象
     */
    public static MipsOperand buildOperand(Value irValue, boolean isImm, Function irFunction, BasicBlock irBlock) {
        MipsOperand op = MipsContext.getOperand(irValue);
        // 1. op不是null，说明已经被解析（映射）过了，也就是作为右操作数
        if (op != null) {
            // 1.1 不需要立即数，并且op是立即数，则需要move到寄存器中
//            if (!isImm && op instanceof MipsImm) {
//                System.out.println("Gary非常疑惑的buildOperand: " + op + ", isImm: " + isImm);
//                // 1.1.1 被解析过的op是0
//                if (((MipsImm) op).getValue() == 0) {
//                    return MipsRReg.ZERO;
//                }
//                // 1.1.2 被解析过的op非0，调用move指令
//                else {
//                    MipsOperand dstVReg = allocateVReg(irFunction);
//                    buildMove(dstVReg, op, irBlock);
//                    return dstVReg;
//                }
//            }
//            // 1.2 不需要立即数，直接返回
//            else {
                return op;
//            }
        }
        // 2. op是null，说明还没有被解析过，要根据类型进行解析
        else {
            // 2.1 全局变量
            if (irValue instanceof GlobalVariable) {
                return buildGVOperand((GlobalVariable) irValue, irFunction, irBlock);
            }
            // 2.2 整型常数
            else if (irValue instanceof ConstInt) {
                return buildImmOperand(((ConstInt) irValue).getValue(), isImm, irFunction, irBlock);
            }
            // 2.3 函数形参
            else if (irValue.isArg() && irFunction.getArgValues().contains(irValue)) {
                return buildArgOperand(irValue, irFunction);
            }
            // 是指令，需要生成一个新的目的寄存器
            else {
                return allocateVReg(irValue, irFunction);
            }
        }
    }

    /**
     * 生成虚拟寄存器，存到对应MipsFunction中。不是基本块，尺度不够大！
     * 一般与move指令配合使用
     * @param irFunction    所处的irFunction
     */
    public static MipsVReg allocateVReg(Function irFunction) {
        MipsVReg vr = new MipsVReg();
        // 向MipsFunction中添加虚拟寄存器
        MipsContext.getFunction(irFunction).addUsedVReg(vr);
        return vr;
    }

    /**
     * 生成虚拟寄存器，存到对应MipsFunction中。
     * 与irValue存在映射，需要记录该映射关系
     */
    public static MipsVReg allocateVReg(Value irValue, Function irFunction) {
        // 先分配一个虚拟寄存器
        MipsVReg vr = allocateVReg(irFunction);
        if (irValue != null) {
            MipsContext.addOperandMap(irValue, vr);
        }
        return vr;
    }

    /**
     * 构建全局变量操作数
     */
    public static MipsOperand buildGVOperand(GlobalVariable irValue, Function irFunction, BasicBlock irBlock) {
        MipsVReg dst = allocateVReg(irFunction);
        buildMove(dst, new MipsLabel(irValue.getNameCnt()), irBlock);
        return dst;
    }

    /**
     * 给定立即数，创建MipsOperand对象，区分是返回寄存器还是立即数
     * @param immVal     立即数值
     * @param isImm      是否需要立即数op
     */
    public static MipsOperand buildImmOperand(int immVal, boolean isImm, Function irFunction, BasicBlock irBlock) {
        MipsImm mipsImm = new MipsImm(immVal);
        // 若允许返回立即数对象，则立即返回即可
        if (isImm && MathUtils.is16BitImm(immVal, true)) {
            return mipsImm;
        }
        // 不允许返回立即数，则返回寄存器
        else {
            // 若立即数为0，则返回0寄存器
            if (immVal == 0) {
                return MipsRReg.ZERO;
            }
            // 若立即数非0，则调用move指令
            else {
                MipsVReg dstVReg = allocateVReg(irFunction);
                buildMove(dstVReg, mipsImm, irBlock);
                return dstVReg;
            }
        }
    }

    /**
     * 给定函数形参，创建MipsOperand对象
     * @param irArg     函数参数（应该是形参）
     */
    public static MipsOperand buildArgOperand(Value irArg, Function irFunction) {
        MipsFunction mipsFunction = MipsContext.getFunction(irFunction);
        // 获取参数的序号。规定从0开始编号
        int argId = irArg.getArgId();
        // 获取函数中打头的基本块
        MipsBlock firstBlock = MipsContext.getBasicBlock(irFunction.getHeadBlock());
        // 分配虚拟寄存器
        MipsVReg argReg = allocateVReg(irFunction);

        // 参数编号小于4，存入a0~a3
        if (argId < 4) {
            // 创建move指令，将参数存入寄存器$a0~$a3
            MipsMove move = new MipsMove(argReg, new MipsRReg("a" + argId));
            // 在函数最开头加入move指令： move	v8,	$a0
            firstBlock.addInstructionFirst(move);
        }
        // 参数编号大于等于4，存入栈上
        else {
            // 计算参数在栈上的偏移量
            int stackOffset = argId - 4;
            // 相对于参数区的偏移量，FIXME: 该参数在函数中还会修改
            MipsImm offsetImm = new MipsImm(stackOffset * 4);
            // 加入函数参数偏移量列表
            mipsFunction.addArgOffset(offsetImm);
            // 创建加载指令：从栈上加载参数到寄存器，lw v13, 4($sp)
            MipsLoad load = new MipsLoad(argReg, MipsRReg.SP, offsetImm);
            // 在函数最开头加入load指令
            firstBlock.addInstructionFirst(load);
        }
        return argReg;
    }
}
