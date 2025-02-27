package backend.operands;

import java.util.Objects;

/**
 * @author Gary
 * @Description: 标签类型，在buildGVOperand中用到，就是全局变量的名字
 * @date 2024/12/5 21:14
 */
public class MipsLabel extends MipsOperand {
    private String name;

    public MipsLabel(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsLabel objLabel = (MipsLabel) o;
        return Objects.equals(name, objLabel.name);
    }
}
