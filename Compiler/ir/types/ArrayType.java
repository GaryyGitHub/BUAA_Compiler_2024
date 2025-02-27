package ir.types;

/**
 * @author Gary
 * @Description: 一维数组类型
 * @date 2024/11/17 20:29
 */
public class ArrayType extends ValueType {
    // ============ 成员变量 ============
    // 数组元素类型
    private ValueType elementType;
    // 数组长度
    private int length;
    // 数组所占空间大小（bit）
    private int size;

    // ============ getter/setter ============
    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getSize(boolean isMips) {
        if (!isMips) {
            return size;
        }
        // 如果是char类型，需要乘以4
        if (elementType.getSize() == 1) {
            return size * 4;
        }
        return size;
    }

    public ValueType getElementType() {
        return elementType;
    }

    // ============ 构造函数 ============
    public ArrayType(ValueType elementType, int length) {
        this.elementType = elementType;
        this.length = length;
        this.size = length * elementType.getSize();
    }

    public String toString() {
        return "[" + length + " x " + elementType + "]";
    }
}
