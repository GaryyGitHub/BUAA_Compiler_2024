package ir;

import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.ConstString;
import ir.values.constants.Constant;
import ir.values.instructions.*;
import nodes.CompUnitNode;
import utils.IOUtils;
import utils.IrUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Gary
 * @Description: LLVM IR 中间代码生成的控制器类, Compiler从此调用
 * @date 2024/11/17 16:20
 */
public class IrBuilder {
    private final CompUnitNode compUnitNode;

    public IrBuilder(CompUnitNode compUnitNode) {
        this.compUnitNode = compUnitNode;
    }

    // 生成中间代码的主要方法
    public void generate() {
        // ============= 生成中间代码 =============
        compUnitNode.buildIr();
    }

    public void printResult() {
//        System.out.println("没问题");
        System.out.println(">>>>> 开始输出中间代码 >>>>>");
        IOUtils.write(Module.getInstance().toString(), IOUtils.llvmFile);
        System.out.println(">>>>> 输出中间代码OK >>>>>");
    }

    public Module getIrModule() {
        return Module.getInstance();
    }

    // ============= 命名计数器 =============
    private static int nameCnt = 0;
    // 实际上，许多不需要新分配虚拟寄存器的指令也调用了这一函数，会导致有些nameCnt并没用上
    // 此外，block、局部变量都共用nameCnt，这一特性不好解决，不如不解决
    private static String getNameString() {
        return "" + nameCnt++;
    }
    // 初始化命名计数器
    public static void initNameCnt() {
        nameCnt = 0;
    }

    // ============= 格式化字符串命名计数器 =============
    private static int formatStrCnt = 0;

    private static String getFormatStrName() {
        return "FORMAT_STRING_" + formatStrCnt++;
    }

    // ============= 各类builder函数定义 =============
    /**
     * 新建函数定义的指令
     * 将函数加入Module的函数列表和IrSymTableStack的全局符号表中
     */
    public static Function buildFunction(String name, ValueType returnType, ArrayList<ValueType> argTypes, boolean isLibFunc) {
        initNameCnt();  // 每进入一个新函数，就初始化命名计数器，从0开始！
        Function func = new Function(name, returnType, argTypes, isLibFunc);
        Module.addFunction(func);
        IrSymTableStack.addGlobalSymTable(name, func);
        return func;
    }

    /**
     * 创建函数的下属基本快, 除了由函数本身产生，基本就是if和for了
     * 会将其加入函数的基本块列表
     */
    public static BasicBlock buildBasicBlock(Function function) {
        BasicBlock basicBlock = new BasicBlock(getNameString(), function);
        function.addBlock(basicBlock);
        return basicBlock;
    }

    /**
     * 带指定初始值的全局变量
     * name - 变量标识符名，isConst - 是否是常量，initVal - 初始值
     */
    public static GlobalVariable buildGlobalVariable(String name, boolean isConst, Constant initVal) {
        GlobalVariable globalVariable = new GlobalVariable(name, isConst, initVal);
        Module.addGlobalVariable(globalVariable);   // 加入Module的全局变量列表
        IrSymTableStack.addGlobalSymTable(name, globalVariable);    // 加入全局符号表
        return globalVariable;
    }

    // 存储已有的字符串。若printf时出现相同字符串，只存一个。键为字符串，值为全局变量。
    private static HashMap<String, GlobalVariable> formatStrMap = new HashMap<>();

    /**
     * 在printf中用到，用来创建格式化字符串的全局变量
     * @param formatStr   格式化字符串
     */
    public static GlobalVariable buildGlobalConstString(String formatStr) {
        if (formatStrMap.containsKey(formatStr)) {
            // 若已有相同的字符串，直接返回即可
            return formatStrMap.get(formatStr);
        }
        GlobalVariable strGlobalVar = buildGlobalVariable(getFormatStrName(), true, new ConstString(formatStr));
        formatStrMap.put(formatStr, strGlobalVar);
        return strGlobalVar;
    }

    /**
     * 创建返回指令, 会将该指令加入基本块
     */
    public static void buildRetInstruction(BasicBlock parent, Value retVal) {
        // int or char类型返回值
        // FIXME: 目前只支持int类型，char类型咋办？？？
        if (retVal != null) {
            int funcBits = ((IntType)(IrContext.curFunction.getReturnType())).getBits();
            int retValBits = ((IntType)(retVal.getType())).getBits();
//            System.out.println("funcBits: " + funcBits + " retValBits: " + retValBits);
            // 如果返回值类型和函数类型不一致，要让返回值类型向函数类型看齐！！
            if (retValBits < funcBits) {
                // 扩展返回值类型
                retVal = buildZextInstruction(retVal, parent);
            }
            if (retValBits > funcBits) {
                // 缩短返回值类型
                retVal = buildTruncInstruction(retVal, parent);
            }
            parent.addInstruction(new Ret(parent, retVal));
        }
        // void类型返回值
        else {
            parent.addInstruction(new Ret(parent));
        }
    }

    /**
     * 在函数的开头，创建内存分配指令，不带有const初值
     * @param pointingType 要存储的类型
     * @param parent       所在基本块
     * @return alloca     指向的指针
     */
    public static Alloca buildAllocaInstruction (ValueType pointingType, BasicBlock parent) {
        // 把所有内存分配放在函数的开头
        BasicBlock funcHeadBlock = IrUtils.getHeadBBOfParentFunction(parent);
        Alloca alloca = new Alloca(getNameString(), pointingType, funcHeadBlock);
        // 不仅插入函数开头的基本块，还要插入到这个基本块的开头
        funcHeadBlock.addInstructionAtHead(alloca);
        return alloca;
    }

    /**
     * 在函数的开头，创建内存分配指令，应该只在constDef中使用
     * <result> = alloca <type>
     * @param pointingType 要存储的类型
     * @param parent       所在基本块
     * @param constArray   常量初值
     */
    public static Alloca buildAllocaInstruction (ValueType pointingType, BasicBlock parent, ConstArray constArray) {
        // 把所有内存分配放在函数的开头
        BasicBlock funcHeadBlock = IrUtils.getHeadBBOfParentFunction(parent);
        // 构造alloca指令，注意getNameString()的作用，后续在调super时会在它前面加上"%i"
        Alloca alloca = new Alloca(getNameString(), pointingType, funcHeadBlock, constArray);
        // 不仅插入函数开头的基本块，还要插入到这个基本块的开头
        funcHeadBlock.addInstructionAtHead(alloca);
        return alloca;
    }

    /**
     * 构建getelementptr - store指令，并且把定义的初值存入局部数组
     * @param arrayPointer  目标数组指针
     * @param length        数组长度
     * @param values        要存储的value数组
     * @param parent        所在基本块
     */
    public static void buildStoreArrayValues(int intBits, Alloca arrayPointer, int length, ArrayList<Value> values, BasicBlock parent) {
        // 获取一个指向底层元素的指针，挨个存入元素
        GetElementPtr basePtr = IrBuilder.buildRankDownInstruction(intBits, arrayPointer, parent);
        // 遍历数组，依次把元素使用store存储，存储位置是base+i
        GetElementPtr elementPtr = basePtr;
        IrBuilder.buildStoreInstruction(values.get(0), elementPtr, parent);
        for (int i = 1; i < values.size(); i++) {
            elementPtr = IrBuilder.buildGetElementPtrInstruction(basePtr, new ConstInt(intBits, i), parent);
            IrBuilder.buildStoreInstruction(values.get(i), elementPtr, parent);
        }
    }

    // TODO: 加减乘除这类指令其实没有必要设置intBits，都默认设成了32位
    /**
     * 构建加法指令
     */
    public static Add buildAddInstruction(Value op1, Value op2, BasicBlock parent) {
        // FIXME: op1和op2的位数可能不是32，必须统一转换成i32
        int op1Bits = ( (IntType) (op1.getType()) ).getBits();
        int op2Bits = ( (IntType) (op2.getType()) ).getBits();
//        System.out.println("before add: op1Bits = " + op1.getName() + " " + op1Bits);
//        System.out.println("before add: op2Bits = " + op2.getName() + " " + op2Bits);
        // op1不是i32的情况
        if (op1Bits < 32) {
            op1 = buildZextInstruction(op1, IrContext.curBlock);
        }
        // op2不是i32的情况
        if (op2Bits < 32) {
            op2 = buildZextInstruction(op2, IrContext.curBlock);
        }
        Add add = new Add(getNameString(), parent, op1, op2);
        parent.addInstruction(add);
        return add;
    }

    /**
     * 构建减法指令
     * @param op1        被减数
     * @param op2        减数
     * @param parent     所在基本块
     * @return          sub减法指令
     */
    public static Sub buildSubInstruction(Value op1, Value op2, BasicBlock parent) {
        int op1Bits = ( (IntType) (op1.getType()) ).getBits();
        int op2Bits = ( (IntType) (op2.getType()) ).getBits();
//        System.out.println("before sub: op1Bits = " + op1.getName() + " " + op1Bits);
//        System.out.println("before sub: op2Bits = " + op2.getName() + " " + op2Bits);
        // op1不是i32的情况
        if (op1Bits < 32) {
            op1 = buildZextInstruction(op1, IrContext.curBlock);
        }
        // op2不是i32的情况
        if (op2Bits < 32) {
            op2 = buildZextInstruction(op2, IrContext.curBlock);
        }
        Sub sub = new Sub(getNameString(), parent, op1, op2);
        parent.addInstruction(sub);
        return sub;
    }

    /**
     * 构建乘法指令
     */
    public static Mul buildMulInstruction(Value op1, Value op2, BasicBlock parent) {
        int op1Bits = ( (IntType) (op1.getType()) ).getBits();
        int op2Bits = ( (IntType) (op2.getType()) ).getBits();
//        System.out.println("before mul: op1Bits = " + op1.getName() + " " + op1Bits);
//        System.out.println("before mul: op2Bits = " + op2.getName() + " " + op2Bits);
        // op1不是i32的情况
        if (op1Bits < 32) {
            op1 = buildZextInstruction(op1, IrContext.curBlock);
        }
        // op2不是i32的情况
        if (op2Bits < 32) {
            op2 = buildZextInstruction(op2, IrContext.curBlock);
        }
        Mul mul = new Mul(getNameString(), parent, op1, op2);
        parent.addInstruction(mul);
        return mul;
    }

    /**
     * 构建除法指令
     */
    public static Sdiv buildSdivInstruction(Value op1, Value op2, BasicBlock parent) {
        int op1Bits = ( (IntType) (op1.getType()) ).getBits();
        int op2Bits = ( (IntType) (op2.getType()) ).getBits();
//        System.out.println("before sdiv: op1Bits = " + op1.getName() + " " + op1Bits);
//        System.out.println("before sdiv: op2Bits = " + op2.getName() + " " + op2Bits);
        // op1不是i32的情况
        if (op1Bits < 32) {
            op1 = buildZextInstruction(op1, IrContext.curBlock);
        }
        // op2不是i32的情况
        if (op2Bits < 32) {
            op2 = buildZextInstruction(op2, IrContext.curBlock);
        }
        Sdiv sdiv = new Sdiv(getNameString(), parent, op1, op2);
        parent.addInstruction(sdiv);
        return sdiv;
    }


    public static Srem buildSremInstruction(Value op1, Value op2, BasicBlock parent) {
        int op1Bits = ( (IntType) (op1.getType()) ).getBits();
        int op2Bits = ( (IntType) (op2.getType()) ).getBits();
//        System.out.println("before srem: op1Bits = " + op1.getName() + " " + op1Bits);
//        System.out.println("before srem: op2Bits = " + op2.getName() + " " + op2Bits);
        // op1不是i32的情况
        if (op1Bits < 32) {
            op1 = buildZextInstruction(op1, IrContext.curBlock);
        }
        // op2不是i32的情况
        if (op2Bits < 32) {
            op2 = buildZextInstruction(op2, IrContext.curBlock);
        }
        Srem srem = new Srem(getNameString(), parent, op1, op2);
        parent.addInstruction(srem);
        return srem;
    }

    /**
     * 构建降低一维的gep指令
     * @param ptrval    基地址
     * @param parent
     */
    public static GetElementPtr buildRankDownInstruction (int intBits, Value ptrval, BasicBlock parent) {
        // 此处的ZERO就是象征着一个数，值为多少不重要，重要的是代表了整数类型
        return buildGetElementPtrInstruction(ptrval, ConstInt.ZERO(intBits), ConstInt.ZERO(intBits), parent);
    }

    /**
     * 构建带有二维寻址的gep指令，返回一个降一级的指针
     * <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
     * @param ptrval    基地址
     * @param index1    本维寻址
     * @param index2    下一维寻址
     */
    public static GetElementPtr buildGetElementPtrInstruction (Value ptrval, Value index1, Value index2, BasicBlock parent) {
        GetElementPtr gep = new GetElementPtr(getNameString(), parent, ptrval, index1, index2);
        parent.addInstruction(gep);
        return gep;
    }

    /**
     * 构建带有本维寻址的gep指令
     * 返回的指针是同级的，即向前挪动index
     * <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
     * @param ptrval    基地址
     * @param index     本维寻址
     */
    public static GetElementPtr buildGetElementPtrInstruction (Value ptrval, Value index, BasicBlock parent) {
        GetElementPtr gep = new GetElementPtr(getNameString(), parent, ptrval, index);
        parent.addInstruction(gep);
        return gep;
    }

    /**
     * 创建store指令，把value存入pointer指向的地址
     * store <ty> <value>, ptr <pointer>
     * @param value     要存储的内容
     * @param pointer   存入的地址
     * @param parent    所在基本块
     */
    public static void buildStoreInstruction (Value value, Value pointer, BasicBlock parent) {
        // FIXME: value和pointer的位数可能不同，需要进行类型转换。value要始终遵循pointer的位数
        if (!(value.getType() instanceof PointerType)) {
            int valueBits = ( (IntType) (value.getType()) ).getBits();
            int pointerBits = ((IntType) (( (PointerType) (pointer.getType()) ).pointingType)).getBits();
            System.out.println("before store: valueBits = " + valueBits);
            System.out.println("before store: pointerBits = " + pointerBits);
            // 这是i32 -> i8的情况
            if (valueBits > pointerBits) {
                value = buildTruncInstruction(value, IrContext.curBlock);
            }
            // 这是i8 -> i32的情况
            if (pointerBits > valueBits) {
                value = buildZextInstruction(value, IrContext.curBlock);
            }
        }
        Store store = new Store(getNameString(), parent, value, pointer);
        parent.addInstruction(store);
    }

    /**
     * 无条件跳转指令
     * br label <dest>
     */
    public static void buildBrInstruction (BasicBlock target, BasicBlock parent) {
        Br br = new Br(parent, target);
        parent.addInstruction(br);
    }

    /**
     * 有条件跳转指令
     * @param cond           跳转条件
     * @param trueBranch     条件为真，跳转到的目标基本块
     * @param falseBranch    条件为假，跳转到的目标基本块
     */
    public static void buildBrInstruction (Value cond, BasicBlock trueBranch, BasicBlock falseBranch, BasicBlock parent) {
        Br br = new Br(parent, cond, trueBranch, falseBranch);
        parent.addInstruction(br);
    }

    /**
     * 构建函数调用指令
     * <result> = call [ret attrs] <ty> <name>(<...args>)
     * @param callee    被调用的函数
     * @param rArgs     实参列表
     * @param parent    所在基本块
     */
    public static Call buildCallInstruction (Function callee, ArrayList<Value> rArgs, BasicBlock parent) {
        for (int i = 0; i < rArgs.size(); i++){
            ValueType rArg = rArgs.get(i).getType();  // 实参
            ValueType fArg = callee.getArgValues().get(i).getType();  // 形参
            if (!Objects.equals(rArg.toString(), fArg.toString())) {
                // 实参和形参类型不同，需要把实参类型往形参类型类型转换，其实只涉及i32->i8和i8->i32两种情况
                if (rArg instanceof IntType && fArg instanceof IntType) {
                    int rArgBits = ( (IntType) rArg ).getBits();
                    int fArgBits = ( (IntType) fArg ).getBits();
                    if (rArgBits > fArgBits) {
                        // 实参需要进行i32->i8的转换
                        rArgs.set(i, buildTruncInstruction(rArgs.get(i), parent));
                    }
                    if (rArgBits < fArgBits) {
                        // 实参需要进行i8->i32的转换
                        rArgs.set(i, buildZextInstruction(rArgs.get(i), parent));
                    }
                } else {
                    System.out.println("出现错误：实参和形参类型不同，并且二者至少有一个不是IntType");
                }
            }
//            System.out.println("before call: 实参 " + rArg + " 形参 = " + fArg + (Objects.equals(rArg.toString(), fArg.toString())));
        }
        Call call = new Call(getNameString(), parent, callee, rArgs);
        parent.addInstruction(call);
        return call;
    }

    /**
     * 构建加载指令，从pointer处
     * @param pointer
     * @param parent
     */
    public static Load buildLoadInstruction (Value pointer, BasicBlock parent) {
        Load load = new Load(getNameString(), parent, pointer);
        parent.addInstruction(load);
        return load;
    }

    /**
     * 构建比较指令，比较op1和op2是否满足condType的条件
     * <result> = icmp <cond> <ty> <op1>, <op2>
     * @param condType  比较类型，是Icmp类中的内部枚举类
     * @return
     */
    public static Icmp buildICmpInstruction (Value op1, Value op2, Icmp.CondType condType, BasicBlock parent) {
//        System.out.println("icmpppppppp " + op1.getType() + " " + op2.getType());
        int op1Bits = ( (IntType) (op1.getType()) ).getBits();
        int op2Bits = ( (IntType) (op2.getType()) ).getBits();
        // icmp也需要把i8增广为i32
        if (op1Bits < 32) {
            op1 = buildZextInstruction(op1, IrContext.curBlock);
        }
        // op2不是i32的情况
        if (op2Bits < 32) {
            op2 = buildZextInstruction(op2, IrContext.curBlock);
        }
        Icmp icmp = new Icmp(getNameString(), parent, condType, op1, op2);
        parent.addInstruction(icmp);
        return icmp;
    }

    /**
     * 强制构建从i1或i8扩展到i32的指令
     * <result> = zext <ty> <value> to <ty2>
     * @param op        要从i1或i8扩展到i32的value
     * @return          转换指令（valueType为IntType(i32)）
     */
    public static Value buildZextInstruction (Value op, BasicBlock parent) {
        Zext zext = new Zext(getNameString(), parent, op);
        parent.addInstruction(zext);
        return zext;
    }

    /**
     * 强制构建从i32截断到i8的指令
     * <result> = trunc <ty> <value> to <ty2>
     * @param op        要从i32截断到i8的value
     * @return          转换指令（valueType为IntType(i8)）
     */
    public static Value buildTruncInstruction (Value op, BasicBlock parent) {
        Trunc trunc = new Trunc(getNameString(), parent, op);
        parent.addInstruction(trunc);
        return trunc;
    }
}
