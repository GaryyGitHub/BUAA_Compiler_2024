package utils;

import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;

import javax.crypto.spec.PSource;
import java.util.ArrayList;

/**
 * @author Gary
 * @Description: 中间代码生成所用到的工具类
 * @date 2024/11/18 17:47
 */
public class IrUtils {
    /**
     * 用在alloca指令中：获取父亲函数对象中的第一个基本块
     */
    public static BasicBlock getHeadBBOfParentFunction(BasicBlock bb) {
        return bb.getParentFunction().getHeadBlock();
    }

    /**
     * 给定一个指向数组的指针类型Value, 返回其所指向的数组的elementType
     * @param pointer   指向数组的指针
     * @return        pointer指向的数组的elementType
     */
    public static ValueType getElementTypeOfArrayPointer(Value pointer) {
        ValueType type = getPointingTypeOfPointer(pointer);
        if (type instanceof ArrayType) {
            return ((ArrayType) type).getElementType();
        }
        System.out.println("GaryError: in IrUtils.getElementTypeOfArrayPointer, 尝试获取非数组指针所指向的数组元素类型");
        System.out.println("" + pointer + pointer.getType());
        return new VoidType();
    }

    /**
     * 返回一个指针类型的Value 所**指向**的类型
     * @param pointer   指针value
     * @return       pointer所指向的ValueType
     */
    public static ValueType getPointingTypeOfPointer(Value pointer) {
        ValueType type = pointer.getType();
        if (type instanceof PointerType) {
            return ((PointerType) type).pointingType;
        }
        System.out.println("GaryError: in IrUtils.getPointingTypeOfPointer, 尝试获取非指针对象所指向的类型");
        return new VoidType();
    }

    /**
     * 返回格式化字符串的长度
     */
    public static int getFormatStrLen(String str) {
        String res = str.replace("\\0a", "r");
        return res.length();
    }

    /**
     * 将格式化字符串分割成多个字符串，以%d/%c为分隔符
     * @param str    待分割的格式化字符串
     * @return       分割后的字符串列表
     */
    public static ArrayList<String> splitFormatString(String str) {
        // 这个str字符串开头结尾是带有引号的！这顿操作把引号忽略了
        ArrayList<String> res = new ArrayList<>();
        // 将\n换成\0a
        str = str.replace("\\n", "\\0a");
        int front = 1, len = str.length() - 1;
        for (int i = 1; i < len; i++) {
            if (i + 1 < len && ((str.charAt(i) == '%' && str.charAt(i + 1) == 'd') || (str.charAt(i) == '%' && str.charAt(i + 1) == 'c'))) {
                // 不是两个%d/%c相连的情况，把前面的字符串添加到res中
                if (front != i) {
                    res.add(str.substring(front, i));
                }
                // 把%d/%c添加到res中
                res.add(str.substring(i, i + 2));
                front = i + 2;
                i++;
            }
        }
        // 如果不以%d/%c结尾，还要把最后的字符串添加到res中
        if (front < len) {
            res.add(str.substring(front, len));
        }
        return res;
    }

    /**
     * 在Ir阶段的toString()处理中，去掉stringBuilder末尾的逗号和空格", "
     */
    public static void cutSBTailComma(StringBuilder sb) {
        int len = sb.length();
        // 如果sb的最后两个字符是", "，则删去即可
        if (len >= 2 && sb.charAt(len-2) == ',' && sb.charAt(len-1) == ' ') {
            sb.delete(len-2, len);
        }
    }

    /**
     * 对于一个value，拼接成 type + " " + name 形式的字符串
     */
    public static String typeAndNameStr(Value value) {
        return value.getType() + " " + value.getName();
    }

    /**
     * 给sb添加一个 type + " " + name 形式的实参列表，删去末尾的逗号和空格
     * 形如：[3 x i32]* %13, i32 0, i32 1
     * @param sb      StringBuilder对象
     * @param params  参数列表
     */
    public static void appendSBParamList(StringBuilder sb, ArrayList<Value> params) {
        if (!params.isEmpty()) {
            for (Value param : params) {
                sb.append(typeAndNameStr(param)).append(", ");
            }
            // 删去末尾的逗号和空格
            cutSBTailComma(sb);
        }
    }

    /**
     * 获取一个ConstInt类型的Value的常量值
     */
    public static int getConstIntValue(Value value) {
        return ((ConstInt) value).getValue();
    }
}
