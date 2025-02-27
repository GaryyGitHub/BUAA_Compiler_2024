package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.GlobalVariable;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.instructions.Alloca;
import utils.IOUtils;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 左值表达式
 * @date 2024/10/13 10:53
 * LVal -> Ident ['[' Exp ']']
 */
public class LValNode {
    private Token ident;
    private Token leftBracket;
    private ExpNode expNode;
    private Token rightBracket;

    public Token getIdent() {
        return ident;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public LValNode(Token ident, Token leftBracket, ExpNode expNode, Token rightBracket) {
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.expNode = expNode;
        this.rightBracket = rightBracket;
    }

    public void print() {
        IOUtils.write(ident.toString());
        if (leftBracket != null) {
            IOUtils.write(leftBracket.toString());
            expNode.print();
            IOUtils.write(rightBracket.toString());
        }
        IOUtils.write("<LVal>\n");
    }

    // LVal -> Ident ['[' Exp ']']
    /**
     * LVal返回指针类型的Value，该指针是所求变量的地址。
     * 让上层PrimaryExp来判断是否进行加载。
     * 对于函数实参，其降维操作在此执行
     */
    public void buildIr() {
        // 查符号表，获得左值对应的value
        Value lValValue = IrSymTableStack.getSymbol(ident.getValue());

        // I. 左值为IntType，不需要进行取值
        if (lValValue.getType() instanceof IntType) {
            /*
             * 如果isBuildingConstExp（无变量常数表达式），那么一定返回synInt
             * 因为常量只需计算值，不会出现ident。
             * 否则，给synValue存上value
             */
            if (IrContext.isBuildingConstExp) {
                IrContext.synInt = ((ConstInt) lValValue).getValue();
            } else {
                IrContext.synValue = lValValue;
            }
        }
        // II. 左值为Pointer类型，需要进行取值
        else {
            ValueType valueType = IrUtils.getPointingTypeOfPointer(lValValue);
            // 依据指针所指向的类型分类讨论
            // 1. 指向int/char类型
            if (valueType instanceof IntType) {
                if (IrContext.isBuildingConstExp && lValValue instanceof GlobalVariable) {
                    // 全局常量的值一定是常数表达式，用synInt传递
                    // FIXME: 为什么是全局常量？为何不是全局变量或局部常量？
                    ConstInt initValue = (ConstInt) (((GlobalVariable) lValValue).getInitValue());
                    IrContext.synInt = initValue.getValue();
                } else {
                    IrContext.synValue = lValValue;
                }
            }
            /*
             * 2. 指向指针类型，这个指针一定是当前左值所处函数里面的【数组形参】。
             * 例如定义了f(a[])，在其中访问a中元素就会用到这个指针。
             * 故不可能在buildingConstExp. lValValue只可能是一维数组 i32*
             * 只有形参在满足SSA的时候 会通过alloca和store 来在本来的指针上多附加一层指针 以存储形参指针的值
             */
            // FIXME: 这里的处理方式是否正确？
            else if(valueType instanceof PointerType) {
                // 复原指针所指向的形参
                Value fParamValue = IrBuilder.buildLoadInstruction(lValValue, IrContext.curBlock);
                if (expNode == null) {
                    // 1. 不是数组
                    IrContext.synValue = fParamValue;
                } else {
                    // 2. 数组
                    expNode.buildIr();
                    // 得到了数组下标
                    Value indexValue = IrContext.synValue;
                    // 根据index值通过gep指令取数组的值
                    Value ptrValue = IrBuilder.buildGetElementPtrInstruction(fParamValue, indexValue, IrContext.curBlock);
                    IrContext.synValue = ptrValue;
                }
            }
            // 3. 指向数组，是正常的局部或全局数组
            else if (valueType instanceof ArrayType) {
                // 3.1 常量数组，最后的结果一定是ConstInt
                // 全局和局部都已存储在相应对象内，直接读取即可
                // 返回synInt
                if (IrContext.isBuildingConstExp) {
                    Constant initVal;
                    // 3.1.1 全局常量数组，应为GlobalVariable形式
                    if (lValValue instanceof GlobalVariable) {
                        initVal = ((GlobalVariable) lValValue).getInitValue();
                    }
                    // 3.1.2 局部常量数组，应为alloca形式
                    else {
                        initVal = ((Alloca) lValValue).getInitArray();
                    }
                    expNode.buildIr();
                    // 得到了数组下标
                    initVal = ((ConstArray) initVal).getElements().get(IrContext.synInt);
                    IrContext.synInt = ((ConstInt) initVal).getValue();
                }
                // 3.2 非常量数组，不再有存储好的初值调用，因此需要使用GEP指令来取值
                // 返回指针synValue
                else {
                    // 根据[]使用gep向下取值，这是调用f(a[1])的情况
                    if (expNode != null) {
                        expNode.buildIr();
                        lValValue = IrBuilder.buildGetElementPtrInstruction(lValValue, ConstInt.ZERO(IrContext.intBits), IrContext.synValue, IrContext.curBlock);
                    }
                    // 特别要注意！这里是调用f(a)的情况，其中a是数组，需要进行降维传参
                    if (IrUtils.getPointingTypeOfPointer(lValValue) instanceof ArrayType) {
                        lValValue = IrBuilder.buildRankDownInstruction(IrContext.intBits, lValValue, IrContext.curBlock);
                    }
                    IrContext.synValue = lValValue;
                }
            }
        }
    }
}
