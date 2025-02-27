package ir.types;

/**
 * @author Gary
 * @Description: 指针类型
 * @date 2024/11/17 20:30
 */
public class PointerType extends ValueType {
    // 指向的类型！
    public ValueType pointingType;
    // 参数为: 指针要指向的类型
    public PointerType(ValueType pointingType) {
        this.pointingType = pointingType;
    }

    // 指针类型大小为 4 字节
    @Override
    public int getSize() {
        return 4;
    }

    public int getSize(boolean isMips) {
        return 4;
    }

    public String toString() {
        return pointingType + "*";
    }
}
