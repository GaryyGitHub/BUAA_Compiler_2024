package nodes;

import ir.IrContext;
import ir.values.constants.ConstInt;
import utils.IOUtils;

/**
 * @author Gary
 * @Description: 常量表达式
 * @date 2024/10/13 10:57
 * ConstExp -> AddExp
 */
public class ConstExpNode {
    private AddExpNode addExp;

    public AddExpNode getAddExpNode() {
        return addExp;
    }

    public ConstExpNode(AddExpNode addExp) {
        this.addExp = addExp;
    }

    public void print() {
        addExp.print();
        IOUtils.write("<ConstExp>\n");
    }

    // ConstExp -> AddExp
    // 向上传递综合属性synInt和synValue
    public void buildIr() {
        IrContext.isBuildingConstExp = true;
        addExp.buildIr();
        IrContext.isBuildingConstExp = false;
        // 构建synValue，因为addExp中并没有考虑常量构建synValue
        IrContext.synValue = new ConstInt(IrContext.intBits, IrContext.synInt);
    }
}
