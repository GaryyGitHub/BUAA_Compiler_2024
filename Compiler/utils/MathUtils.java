package utils;

/**
 * @author Gary
 * @Description: 数学相关工具类，在MIPS中用到
 * @date 2024/12/5 21:30
 */
public class MathUtils {
    /**
     * 能否编码为16位立即数
     * @param isSignExtend   是否扩展到负数
     */
    public static boolean is16BitImm(int imm, boolean isSignExtend) {
        if (isSignExtend) {
            return imm >= -32768 && imm <= 32767;
        } else {
            return imm >= 0 && imm <= 65535;
        }
    }
}
