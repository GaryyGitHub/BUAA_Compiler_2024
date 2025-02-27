package ir.values.constants;

import ir.types.ArrayType;
import ir.types.IntType;
import utils.IrUtils;

/**
 * @author Gary
 * @Description: 字符串常量
 * @date 2024/11/18 11:38
 */
public class ConstString extends Constant {
    private String content;
    public ConstString(String content) {
        // 长度要算上结尾的\00!!
        super(new ArrayType(new IntType(8), IrUtils.getFormatStrLen(content) + 1));
        this.content = content;
    }

    // 形如：c"hello\0a\00"
    public String toString() {
        return " c\"" + content + "\\00\"";
    }

    // 在构建MIPS全局变量时使用，要把llvm的\0a转为\n
    public String getContent() {
        return content.replace("\\0a", "\\n");
    }
}
