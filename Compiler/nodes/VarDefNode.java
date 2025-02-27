package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.ZeroInitializer;
import ir.values.instructions.Alloca;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 变量定义
 * @date 2024/10/13 10:47
 * VarDef -> Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
 */
public class VarDefNode {
    private Token ident;
    private Token leftBracket;
    private ConstExpNode constExpNode;
    private Token rightBracket;
    private Token assign;
    private InitValNode initValNode;

    public Token getIdent() {
        return ident;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public InitValNode getInitValNode() {
        return initValNode;
    }

    public VarDefNode(Token ident, Token leftBracket, ConstExpNode constExpNode, Token rightBracket, Token assign, InitValNode initValNode) {
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.constExpNode = constExpNode;
        this.rightBracket = rightBracket;
        this.assign = assign;
        this.initValNode = initValNode;
    }

    public void print() {
        IOUtils.write(ident.toString());
        // 如果是数组，则打印两个中括号和下标
        if (leftBracket != null) {
            IOUtils.write(leftBracket.toString());
            constExpNode.print();
            IOUtils.write(rightBracket.toString());
        }
        // 如果有初始化值，则打印等号和初始化值
        if (initValNode != null) {
            IOUtils.write(assign.toString());
            initValNode.print();
        }
        IOUtils.write("<VarDef>\n");
    }

    // VarDef -> Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
    // 完成变量声明、赋初值
    public void buildIr() {
        // 1. 非数组变量
        if (constExpNode == null) {
            // 1.1 全局非数组变量
            if (IrSymTableStack.isBuildingGlobalSymTable()) {
                ConstInt initVal;
                if (initValNode == null) {
                    // 未初始化，默认初始化为0
                    initVal = new ConstInt(IrContext.intBits, 0);
                } else {
                    // 初始化,特别注意指导书中强调了：
                    // 在本次的实验中，全局变/常量声明中指定的初值表达式必须是常量表达式(constExp)
                    IrContext.isBuildingConstExp = true;
                    initValNode.buildIr();
                    IrContext.isBuildingConstExp = false;
                    initVal = (ConstInt) IrContext.synValue;
                }
                IrBuilder.buildGlobalVariable(ident.getValue(), false, initVal);
            }
            // 1.2 局部非数组变量
            else {
                // 先分配空间
                Alloca alloca = IrBuilder.buildAllocaInstruction(new IntType(IrContext.intBits), IrContext.curBlock);
                // 将<name, pointer>加入符号表
                IrSymTableStack.addSymToPeek(ident.getValue(), alloca);
                // 若有初值，进行store
                if (initValNode != null) {
                    initValNode.buildIr();
                    IrBuilder.buildStoreInstruction(IrContext.synValue, alloca, IrContext.curBlock);
                }
                // 无初值，则不用管，值未知！！
            }
        }
        // 2. 数组变量
        // Ident '[' ConstExp ']' '=' InitVal
        else {
            // 解析数组长度信息
            constExpNode.buildIr();
            int length = IrContext.synInt;
            ArrayType arrayType = new ArrayType(new IntType(IrContext.intBits), length);
            // 2.1 全局数组变量
            if (IrSymTableStack.isBuildingGlobalSymTable()) {
                // 有初始值
                if (initValNode != null) {
                    initValNode.setLength(length);
                    // 全局变量初始化一定为constExp
                    IrContext.isBuildingConstExp = true;
                    initValNode.buildIr();
                    IrContext.isBuildingConstExp = false;
                    // 全局数组变量初始化
                    IrBuilder.buildGlobalVariable(ident.getValue(), false, (ConstArray) IrContext.synValue);
                }
                // 无初始值，默认初始化为0，用zeroInitializer
                else {
                    ZeroInitializer zeroInitializer = new ZeroInitializer(arrayType);
                    IrBuilder.buildGlobalVariable(ident.getValue(), false, zeroInitializer);
                }
            }
            // 2.2 局部数组变量
            else {
                // 先分配空间
                Alloca arrayPointer = IrBuilder.buildAllocaInstruction(arrayType, IrContext.curBlock);
                // 将<name, pointer>加入符号表
                IrSymTableStack.addSymToPeek(ident.getValue(), arrayPointer);
                // 若有初值，进行store；无初值，注意！只有**局部变量int数组**部分初始化。
                if (initValNode != null) {
                    initValNode.setLength(length);
                    initValNode.buildIr();
                    IrBuilder.buildStoreArrayValues(IrContext.intBits, arrayPointer, length, IrContext.synValueArray, IrContext.curBlock);
                }
                // 局部数组(无论int还是char)无初值，则不用管！！
            }
        }
    }
}
