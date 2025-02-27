package nodes;

import frontend.Token;
import ir.IrBuilder;
import ir.IrContext;
import ir.IrSymTableStack;
import ir.values.Value;
import utils.IOUtils;
import ir.values.instructions.Alloca;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 函数形参表
 * @date 2024/10/13 10:49
 * FuncFParams -> FuncFParam { ',' FuncFParam }
 */
public class FuncFParamsNode {
    private List<FuncFParamNode> funcFParamNodes;
    private List<Token> commas;
    public FuncFParamsNode(List<FuncFParamNode> funcFParamNodes, List<Token> commas) {
        this.funcFParamNodes = funcFParamNodes;
        this.commas = commas;
    }

    public List<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }

    public void print() {
        (funcFParamNodes.get(0)).print();
        for (int i = 1; i < funcFParamNodes.size(); i++) {
            IOUtils.write(commas.get(i-1).toString());
            (funcFParamNodes.get(i)).print();
        }
        IOUtils.write("<FuncFParams>\n");
    }

    // FuncFParams -> FuncFParam { ',' FuncFParam }
    public void buildIr() {
        // 通过调用FuncFParamNode的buildIr方法，把参数加入function对象
        for (int i = 0; i < funcFParamNodes.size(); i++) {
            IrContext.inheritInt = i;
            funcFParamNodes.get(i).buildIr();
        }
        // 下面用刚解析好的函数参数构建SSA形式的函数参数加载指令
        /**
         * 对于一个参数，需要先alloca再store, eg:
         * define dso_local i32 @foo(i32 %arg0, i32* %arg1) {
         * 	%i3 = alloca i32
         * 	%i1 = alloca i32*
         * 	store i32 %arg0, i32* %i1
         * 	store i32* %arg1, i32** %i3
         * }
         */
        // FuncFParamNode中已经把解析完成的参数类型传给curFunction，这里直接取出来
        ArrayList<Value> args = IrContext.curFunction.getArgValues();
        for (int i = 0; i < funcFParamNodes.size(); i++) {
            Value arg = args.get(i);
            // 先alloca指令
            Alloca alloca = IrBuilder.buildAllocaInstruction(arg.getType(), IrContext.curBlock);
            // 再store指令
            IrBuilder.buildStoreInstruction(arg, alloca, IrContext.curBlock);
            // 在符号表中记录形参，对应的value就是alloca的结果，以后再调用需要load！
            IrSymTableStack.addSymToPeek(funcFParamNodes.get(i).getIdent().getValue(), alloca);
        }
    }
}
