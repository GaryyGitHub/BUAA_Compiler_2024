package error;

import symbol.SymbolTable;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * @Description: 错误处理类，单例模式
 * @date 2024/9/24 23:24
 */
public class ErrorHandler {
    private static final ErrorHandler errorHandler = new ErrorHandler();
    // 单例模式
    public static ErrorHandler getInstance() {
        return errorHandler;
    }
    // 错误表
    private List<MyError> myErrorTable = new ArrayList<>();
    // 词法分析 - 将错误信息加入到错误表中
    public void addErrorTable(MyError myError) {
        myErrorTable.add(myError);
    }
    public boolean hasError() {
        return !myErrorTable.isEmpty();
    }
    public void printErrorTable(String filePath) {
        myErrorTable.sort(MyError::compareTo);
        for (MyError myError : myErrorTable) {
            IOUtils.write(myError.toString(), filePath);
            System.out.print(myError);
        }
    }
}
