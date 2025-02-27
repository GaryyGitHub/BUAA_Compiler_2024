package backend.operands;

import javax.swing.text.StringContent;

/**
 * @author Gary
 * @Description: 立即数操作数
 * @date 2024/12/5 17:13
 */
public class MipsImm extends MipsOperand {
    private int value;

    public MipsImm(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String toString() {
        return "" + value;
    }
}
