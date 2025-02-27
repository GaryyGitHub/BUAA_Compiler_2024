package nodes;

import frontend.Token;
import ir.IrContext;
import ir.values.constants.ConstInt;
import utils.IOUtils;
/**
 * @author Gary
 * @Description: 字符
 * @date 2024/10/13 10:53
 * Character -> CharConst
 */
public class CharacterNode {
    private Token charConst;
    public CharacterNode(Token charConst) {
        this.charConst = charConst;
    }

    public void print() {
        IOUtils.write(charConst.toString());
        IOUtils.write("<Character>\n");
    }

    // 计算常数数值只传递synInt
    // 计算变量数值只传递synValue
    public void buildIr() {
        String tmpStr = charConst.getValue();
        String content = tmpStr.substring(1, tmpStr.length() - 1);
        int num = switch (content) {
            case "\\a" -> 7;     // 警告 (bell)
            case "\\b" -> 8;     // 退格
            case "\\t" -> 9;     // 制表符
            case "\\n" -> 10;    // 换行
            case "\\v" -> 11;    // 垂直制表符
            case "\\f" -> 12;    // 换页符
            case "\\0" -> 0;     // 空字符
            case "\\\"" -> 34;   // 双引号
            case "\\'" -> 39;    // 单引号
            case "\\\\" -> 92;   // 反斜杠
            default -> content.charAt(0); // 普通 ASCII 字符
        };
        if (IrContext.isBuildingConstExp) {
            IrContext.synInt = num;
        } else {
            IrContext.synValue = new ConstInt(8, num);
        }
    }
}
