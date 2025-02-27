package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.values.constants.ConstArray;
import ir.values.instructions.Alloca;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 常量定义
 * @date 2024/10/13 10:46
 * ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitVal
 */
public class ConstDefNode {
    private Token ident;
    private Token leftBracket;
    private ConstExpNode constExp;
    private Token rightBracket;
    private Token equalSign;
    private ConstInitValNode constInitValNode;

    public Token getIdent() {
        return ident;
    }

    public ConstExpNode getConstExp() {
        return constExp;
    }

    public ConstInitValNode getConstInitValNode() {
        return constInitValNode;
    }

    public ConstDefNode(Token ident, Token leftBracket, ConstExpNode constExp, Token rightBracket, Token equalSign, ConstInitValNode constInitValNode) {
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.constExp = constExp;
        this.rightBracket = rightBracket;
        this.equalSign = equalSign;
        this.constInitValNode = constInitValNode;
    }

    public void print() {
        IOUtils.write(ident.toString());
        if (leftBracket != null) {
            IOUtils.write(leftBracket.toString());
            constExp.print();
            IOUtils.write(rightBracket.toString());
        }
        IOUtils.write(equalSign.toString());
        constInitValNode.print();
        IOUtils.write("<ConstDef>\n");
    }

    /**
     * ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitVal
     * 常量定义，包含普通变量、一维数组两种情况
     * 1. 对于非数组：无论全局还是局部，都只存在对象中，不写在中间代码里
     * 2. 对于数组：全局数组在最开始记录global，局部数组在函数中用alloca存储
     * 特别地，类型信息由BTypeNode提供，下面传递intBits！！
     */
    public void buildIr() {

        // 1. 非数组常量，一定有后面的ConstInitVal声明
        // Ident '=' ConstInitVal
        if (constExp == null) {
            constInitValNode.buildIr();
            IrSymTableStack.addSymToPeek(ident.getValue(), IrContext.synValue);
        }
        // 2. 数组常量
        // Ident '[' ConstExp ']' '=' ConstInitVal
        else {
            // 解析数组长度信息
            constExp.buildIr();
            int length = IrContext.synInt;
            // 向下传递数组长度信息
            constInitValNode.setLength(length);
            // 获取常量初始化值
            constInitValNode.buildIr();
            /**
             * 根据是否为全局变量，分类处理
             * 1. 全局数组：在IrContext.synValue中存一个ConstArray常量类
             * 2. 局部数组：在IrContext.synValue中存一个ConstArray，在synValueArray存储基本元素
             */
            // 2.1 全局数组，**初值**仅需存在GlobalVariable对象中即可
            if (IrSymTableStack.isBuildingGlobalSymTable()) {
                // true 代表是 const 数组
                IrBuilder.buildGlobalVariable(ident.getValue(), true, (ConstArray) IrContext.synValue);
            }
            // 2.2 局部数组，需要在函数中用alloca存储
            // 操作：手动给数组alloca，然后对元素getelementptr和store
            // 下部会传入构造好的常量数组synValue，以及展平后的synArray
            else {
                // 分配数组空间，注意区分int和char
                ArrayType arrayType = new ArrayType(new IntType(IrContext.intBits), length);
                // 分配空间，同时传入初值，即综合属性 IrContext.synValue
                Alloca arrayPointer = IrBuilder.buildAllocaInstruction(arrayType, IrContext.curBlock, (ConstArray) IrContext.synValue);
                // 将该符号及对应指针存入符号表
                IrSymTableStack.addSymToPeek(ident.getValue(), arrayPointer);
                // 用store和getelementptr指令把内容存入数组
                IrBuilder.buildStoreArrayValues(IrContext.intBits, arrayPointer, length, IrContext.synValueArray, IrContext.curBlock);
            }
        }
    }
}
