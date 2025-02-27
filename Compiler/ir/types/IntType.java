package ir.types;

/**
 * @author Gary
 * @Description: 整数类型，位数可为1、8、32，char先合到int里面吧，后面再看
 * @date 2024/11/17 20:29
 */
public class IntType extends ValueType {
    // int类型的位数，可为1、8、32
    private int bits;

    @Override
    public boolean isI1() {
        return bits == 1;
    }

    @Override
    public int getSize() {
        return bits / 8;
    }

    @Override
    public int getSize(boolean isMips) {
        if (!isMips) {
            return bits / 8;
        }
        // 如果是char类型，需要在原有size上乘4
        if (bits == 8) return bits / 2;
        return bits / 8;
    }

    public int getBits() {
        return bits;
    }

    public IntType(int bits) {
        this.bits = bits;
    }

    public String toString() {
        return "i" + bits;
    }
}
