import backend.MipsBuilder;
import error.ErrorHandler;
import frontend.Lexer;
import frontend.Parser;
import frontend.SemanticAnalysis;
import ir.IrBuilder;
import nodes.CompUnitNode;
import utils.IOUtils;

import java.io.IOException;

/**
 * @author Gary
 * @Description: 编译器main函数
 * @date 2024/9/23 19:41
 */
public class Compiler {
    public static void main(String[] args) throws IOException {
        // 读取文件内容
        String srcCode = IOUtils.read(IOUtils.inputFile);
        // 清空脏输出文件
        IOUtils.clean(IOUtils.llvmFile);
        IOUtils.clean(IOUtils.outputFile);
        IOUtils.clean(IOUtils.errorFile);
        // 定义各类前端分析器
        Lexer lexer = Lexer.getInstance();
        Parser parser = Parser.getInstance();
        SemanticAnalysis semanticAnalysis = SemanticAnalysis.getInstance();
        ErrorHandler errorHandler = ErrorHandler.getInstance();

        /******************* 开始分析 *********************/
        // 词法分析器
        lexer.analyze(srcCode);
        // 语法分析器
        parser.setTokens(lexer.getTokens());    // 设置语法分析器的词法单元流
        parser.analyze();
        // 语义分析器
        semanticAnalysis.CompUnit(parser.getEntry());

        if (!errorHandler.hasError()) {
            // 中间代码生成器
            IrBuilder irBuilder = new IrBuilder(parser.getEntry());
            irBuilder.generate();
            // MIPS代码生成器，使用llvm生成的module作为输入
            MipsBuilder mipsBuilder = new MipsBuilder(irBuilder.getIrModule());
            mipsBuilder.generate();

            /******************* 开始输出 *********************/
//            lexer.printResult();
//            parser.printResult();
//            semanticAnalysis.printResult();
            irBuilder.printResult();
            mipsBuilder.printResult();
        } else {
            errorHandler.printErrorTable(IOUtils.errorFile);
        }
    }
}
