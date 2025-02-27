package nodes;

import frontend.Token;
import ir.IrContext;
import ir.values.constants.ConstInt;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 数值
 * @date 2024/10/13 10:53
 * Number -> IntConst
 */
public class NumberNode {
    Token intConst;
    public NumberNode(Token intConst) {
        this.intConst = intConst;
    }

    public void print() {
        IOUtils.write(intConst.toString());
        IOUtils.write("<Number>\n");
    }

    // 计算常数数值只传递synInt
    // 计算变量数值只传递synValue
    public void buildIr() {
        // 把数字字符串转换成数字
        int num = Integer.parseInt(intConst.getValue());
        if (IrContext.isBuildingConstExp) {
            IrContext.synInt = num;
        } else {
            // FIXME: 这里直接32可不可以？
            IrContext.synValue = new ConstInt(32, num);
        }
    }
}
