package nodes;

import frontend.Token;
import ir.IrContext;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 常量初值
 * @date 2024/10/13 10:46
 * ConstInitVal -> ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
 */
public class ConstInitValNode {
    private List<ConstExpNode> constExpNodes;
    private Token leftBrace;
    private Token rightBrace;
    private List<Token> commas;
    private Token stringConst;

    public List<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    public Token getLeftBrace() {
        return leftBrace;
    }

    public Token getStringConst() {
        return stringConst;
    }

    public ConstInitValNode(List<ConstExpNode> constExpNodes, Token leftBrace, Token rightBrace, List<Token> commas, Token stringConst) {
        this.constExpNodes = constExpNodes;
        this.leftBrace = leftBrace;
        this.rightBrace = rightBrace;
        this.commas = commas;
        this.stringConst = stringConst;
    }

    public void print() {
        if (leftBrace == null) {
            if (stringConst == null) {
                // 情况1：只有一个常量表达式
                constExpNodes.get(0).print();
            } else {
                // 情况3：是字符串常量
                IOUtils.write(stringConst.toString());
            }
        } else {
            // 情况2：是数组常量，可能为空，可能一个，可能多个
            IOUtils.write(leftBrace.toString());
            // 大括号内为空就不用输出了
            if (constExpNodes.size() > 0) {
                constExpNodes.get(0).print();
                for (int i = 1; i < constExpNodes.size(); i++) {
                    IOUtils.write(commas.get(i - 1).toString());
                    constExpNodes.get(i).print();
                }
            }
            IOUtils.write(rightBrace.toString());
        }
        IOUtils.write("<ConstInitVal>\n");
    }

    // 数组长度
    int length;
    public void setLength(int length) {
        this.length = length;
    }

    // ConstInitVal -> ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
    public void buildIr() {
        if (leftBrace == null) {
            if (stringConst == null) {
                // 情况1：只有一个常量表达式 ConstExp,直接向上传递结果
                constExpNodes.get(0).buildIr();
            } else {
                // FIXME: 其实这里的intBits就是8，看看要是有bug再改！
                // 情况3：是字符串常量 StringConst
                ArrayList<Constant> constants = new ArrayList<>();
                ArrayList<Value> values = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    // 要去掉前后的引号！！
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
            // 情况2：是数组常量，可能为空，可能一个，可能多个
            // 需要构建constantInitArray，传递给synValue,用于常量数组的初始化
            ArrayList<Constant> constantExps = new ArrayList<>();
            ArrayList<Value> values = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                if (i < constExpNodes.size()) {
                    constExpNodes.get(i).buildIr();
                    constantExps.add((ConstInt) IrContext.synValue);    // 这个synValue存的就是constExpNode得到的东西
                    values.add(IrContext.synValue);
                } else {
                    constantExps.add(new ConstInt(IrContext.intBits, 0));
                    values.add(new ConstInt(IrContext.intBits, 0));
                }
            }
//            for (ConstExpNode constExpNode : constExpNodes) {
//                constExpNode.buildIr();
//                constantExps.add((ConstInt) IrContext.synValue);    // 这个synValue存的就是constExpNode得到的东西
//                values.add(IrContext.synValue);
//            }
            IrContext.synValue = new ConstArray(constantExps);
            IrContext.synValueArray = values;
        }
    }
}
