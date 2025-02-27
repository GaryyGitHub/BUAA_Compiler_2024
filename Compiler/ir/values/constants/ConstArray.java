package ir.values.constants;

import ir.types.ArrayType;
import utils.IrUtils;

import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 常量数组
 * @date 2024/11/18 11:29
 */
public class ConstArray extends Constant {
    // 存储常量数组
    public ArrayList<Constant> elements = new ArrayList<>();
    // 获取存储的常量数组
    public ArrayList<Constant> getElements() {
        return elements;
    }
    // 常量数组初始化
    public ConstArray(ArrayList<Constant> arr) {
        super (new ArrayType(arr.get(0).getType(), arr.size()), new ArrayList<>(){{
            addAll(arr);
        }});
        elements.addAll(arr);
    }

    // 形如：[i32 1, i32 2, i32 3, i32 4]
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Constant e : elements) {
            sb.append(e.getType()).append(" ").append(e).append(", ");
        }
        // 最后一个逗号不要
        IrUtils.cutSBTailComma(sb);
        sb.append("]");
        return sb.toString();
    }
}
