package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 变量初值
 * @date 2024/10/13 10:48
 * InitVal -> Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
 */
public class InitValNode {
    private List<ExpNode> expNodes;
    private Token leftBrace;
    private Token rightBrace;
    private List<Token> commas;
    private Token stringConst;

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }

    public Token getStringConst() {
        return stringConst;
    }

    public Token getLeftBrace() {
        return leftBrace;
    }

    public InitValNode(List<ExpNode> expNodes, Token leftBrace, Token rightBrace, List<Token> commas, Token stringConst) {
        this.expNodes = expNodes;
        this.leftBrace = leftBrace;
        this.rightBrace = rightBrace;
        this.commas = commas;
        this.stringConst = stringConst;
    }

    public void print() {
        if (leftBrace == null) {
            if (stringConst == null) {
                // 情况1：只有一个常量表达式
                expNodes.get(0).print();
            } else {
                // 情况3：是字符串常量
                IOUtils.write(stringConst.toString());
            }
        } else {
            // 情况2：是数组常量，可能为空，可能一个，可能多个
            IOUtils.write(leftBrace.toString());
            // 大括号内为空就不用输出了
            if (expNodes.size() > 0) {
                expNodes.get(0).print();
                for (int i = 1; i < expNodes.size(); i++) {
                    IOUtils.write(commas.get(i - 1).toString());
                    expNodes.get(i).print();
                }
            }
            IOUtils.write(rightBrace.toString());
        }
        IOUtils.write("<InitVal>\n");
    }

    // 数组长度
    int length;
    public void setLength(int length) {
        this.length = length;
    }

    // InitVal -> Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
    // 在此处将synInt封装为synValue
    public void buildIr() {
        if (leftBrace == null) {
            if (stringConst == null) {
                // 情况1：只有一个常量表达式 Exp,直接向上传递结果
                expNodes.get(0).buildIr();
                if (IrContext.isBuildingConstExp) {
                    IrContext.synValue = new ConstInt(IrContext.intBits, IrContext.synInt);
                }
            } else {
                // 情况3：是字符串常量StringConst，其实这里的intBits就是8
                ArrayList<Constant> constants = new ArrayList<>();
                ArrayList<Value> values = new ArrayList<>();
                // 要跳过前后的引号！！
                for (int i = 0; i < length; i++) {
                    String s = stringConst.getValue().substring(1, stringConst.getValue().length() - 1);
                    if (i < s.length()) {
                        constants.add(new ConstInt(IrContext.intBits, s.charAt(i)));
                        values.add(new ConstInt(IrContext.intBits, s.charAt(i)));
                    } else {
                        constants.add(new ConstInt(IrContext.intBits, 0));
                        values.add(new ConstInt(IrContext.intBits, 0));
                    }
                }
                IrContext.synValue = new ConstArray(constants);
                IrContext.synValueArray = values;
            }
        } else {
            // 情况2：是数组初始化，可能为空，可能一个，可能多个
            ArrayList<Constant> exps = new ArrayList<>();
            ArrayList<Value> values = new ArrayList<>();
            if (IrSymTableStack.isBuildingGlobalSymTable()) {
                // 对于全局变量数组，需要把后面的项都补成0！！！
                for (int i = 0; i < length; i++) {
                    if (i < expNodes.size()) {
                        expNodes.get(i).buildIr();
                        exps.add(new ConstInt(IrContext.intBits, IrContext.synInt));
                        values.add(new ConstInt(IrContext.intBits, IrContext.synInt));
                    } else {
                        exps.add(new ConstInt(IrContext.intBits, 0));
                        values.add(new ConstInt(IrContext.intBits, 0));
                    }
                }
//                for (ExpNode expNode : expNodes) {
//                    expNode.buildIr();
//                    exps.add(new ConstInt(IrContext.intBits, IrContext.synInt));
//                    values.add(new ConstInt(IrContext.intBits, IrContext.synInt));
//                }
                IrContext.synValue = new ConstArray(exps);
                IrContext.synValueArray = values;
            } else {
                // 局部变量数组初始化
                for (int i = 0; i < length; i++) {
                    if (i < expNodes.size()) {
                        expNodes.get(i).buildIr();
                        values.add(IrContext.synValue);
                    } else if (IrContext.intBits == 8) {
                        // 字符数组末尾要特别地补0
                        values.add(new ConstInt(IrContext.intBits, 0));
                    }
                }
//                for (ExpNode expNode : expNodes) {
//                    expNode.buildIr();
//                    System.out.println("synInt - 局部数组初始化: " + IrContext.synValue);
//                    values.add(IrContext.synValue);
//                }
                IrContext.synValueArray = values;
            }
        }
    }
}
