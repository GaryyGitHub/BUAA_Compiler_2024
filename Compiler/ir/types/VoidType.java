package ir.types;

/**
 * @author Gary
 * @Description: 更多用作错误处理
 * @date 2024/11/17 20:31
 */
public class VoidType extends ValueType {

    @Override
    public int getSize() {
        System.out.println("GaryError: [VoidTypeSize] 非法获取Void类型的Size！");
        return 0;
    }

    @Override
    public int getSize(boolean isMips) {
        System.out.println("GaryError: [VoidTypeSize] 非法获取Void类型的Size！而且是在mips中");
        return 0;
    }

    public String toString() {
        return "void";
    }
}
