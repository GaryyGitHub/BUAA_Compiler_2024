package error;

/**
 * @author Gary
 * @Description: 错误类，记录错误信息：行数、种类
 * @date 2024/9/24 23:22
 */
public class MyError {
    private int lineNum;
    private ErrorType errorType;
    public MyError(int lineNum, ErrorType errorType) {
        this.lineNum = lineNum;
        this.errorType = errorType;
    }

    public int compareTo(MyError other) {
        if (lineNum < other.lineNum) {
            return -1;
        } else if (lineNum > other.lineNum) {
            return 1;
        }
        return 0;
    }

    public String toString() {
        return lineNum + " " + errorType.toString() + "\n";
    }
}
