package nodes;

import frontend.Token;
import frontend.TokenType;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 函数类型
 * @date 2024/10/13 11:06
 * FuncType -> 'void' | 'int' | 'char'
 */
public class FuncTypeNode {
    private Token type;

    // 语义分析中，用来获取变量类型
    public TokenType getType() {
        return type.getType();
    }

    public FuncTypeNode(Token type) {
        this.type = type;
    }

    public void print() {
        IOUtils.write(type.toString());
        IOUtils.write("<FuncType>\n");
    }

    // 中间代码生成，获取ValueType类型的返回值类型
    public ValueType getIrReturnType() {
        ValueType vt;
        if (type.getType() == TokenType.VOIDTK) {
            vt = new VoidType();    // void类型
        } else if (type.getType() == TokenType.INTTK) {
            vt = new IntType(32);   // int类型
        } else {
            vt = new IntType(8);    // char类型
        }
        return vt;
    }
}
