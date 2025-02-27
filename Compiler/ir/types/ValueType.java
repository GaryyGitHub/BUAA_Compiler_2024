package ir.types;

/**
 * @author Gary
 * @Description: Value基本类型，是所有类型的基类
 * @date 2024/11/17 19:17
 */
public class ValueType {
    // 占据字节大小
    public int getSize() {
        return 0;
    }

    // 如果是mips调用：要注意，char类型统一成了按4字节存储
    public int getSize(boolean isMips) {
        return 0;
    }

    // int类型的位数
    public int getBits() {
        return 0;
    }

    // 判断是否为i1类型（布尔类型）
    public boolean isI1() {
        return false;
    }
}
