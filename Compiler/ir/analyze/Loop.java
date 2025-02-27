package ir.analyze;

/**
 * @author Gary
 * @Description: 基本块所处的循环的信息
 * @date 2024/11/29 0:55
 */
public class Loop {
    // ============ 成员变量 ============
    private int loopDepth;   // 循环深度，最外层为1

    // ============ 相关方法 ============
    public int getLoopDepth() {
        return loopDepth;
    }
}
