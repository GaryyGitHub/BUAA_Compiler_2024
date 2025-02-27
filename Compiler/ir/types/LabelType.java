package ir.types;

/**
 * @author Gary
 * @Description:
 * @date 2024/11/17 20:30
 */
public class LabelType extends ValueType {

    @Override
    public int getSize() {
        System.out.println("GaryError: [LabelTypeSize] 非法获取Label类型的Size！");
        return 0;
    }

    @Override
    public int getSize(boolean isMips) {
        System.out.println("GaryError: [LabelTypeSize] 非法获取Label类型的Size！而且是在mips中");
        return 0;
    }

    @Override
    public String toString() {
        return "label";
    }
}
