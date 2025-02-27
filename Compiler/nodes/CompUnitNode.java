package nodes;

import ir.IrBuilder;
import ir.IrSymTableStack;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.VoidType;
import ir.values.Function;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 编译单元，是开始符号
 * @date 2024/10/13 0:43
 * CompUnit -> {Decl} {FuncDef} MainFuncDef
 */
public class CompUnitNode {
    private List<DeclNode> declNodes;
    private List<FuncDefNode> funcDefNodes;
    private MainFuncDefNode mainFuncDefNode;
    public CompUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }

    // 下面三个函数在语义分析中用到
    public List<DeclNode> getDeclNodes() {
        return declNodes;
    }

    public List<FuncDefNode> getFuncDefNodes() {
        return funcDefNodes;
    }

    public MainFuncDefNode getMainFuncDefNode() {
        return mainFuncDefNode;
    }

    public void print() {
        // 先打印子程序结果，最后打印顶端的CompUnit
        for (DeclNode declNode : declNodes) {
            declNode.print();
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            funcDefNode.print();
        }
        mainFuncDefNode.print();
        IOUtils.write("<CompUnit>\n");
    }

    // 中间代码生成 CompUnit -> {Decl} {FuncDef} MainFuncDef
    // buildIr()用以进行基于属性翻译文法的递归下降。
    public void buildIr() {
        // 先初始化全局符号表
        IrSymTableStack.init();
        // 在 LLVM IR 头部显式声明IO函数
        Function.getint = IrBuilder.buildFunction("getint", new IntType(32), new ArrayList<>(), true);
        // 特别注意！！getchar和putchar的类型都是i8！！！
        Function.getchar = IrBuilder.buildFunction("getchar", new IntType(32), new ArrayList<>(), true);
        Function.putint = IrBuilder.buildFunction("putint", new VoidType(), new ArrayList<>(){{
            add(new IntType(32));
        }}, true);
        Function.putch = IrBuilder.buildFunction("putch", new VoidType(), new ArrayList<>(){{
            add(new IntType(32));
        }}, true);
        // declare void @putstr(i8*)
        Function.putstr = IrBuilder.buildFunction("putstr", new VoidType(), new ArrayList<>(){{
            add(new PointerType(new IntType(8)));
        }}, true);

        // 访问子节点
        for (DeclNode declNode : declNodes) {
            declNode.buildIr();
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            funcDefNode.buildIr();
        }
        mainFuncDefNode.buildIr();
    }
}
