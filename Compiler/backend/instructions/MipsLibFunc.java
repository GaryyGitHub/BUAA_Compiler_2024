package backend.instructions;

/**
 * @author Gary
 * @Description: 库函数统一调用
 * @date 2024/12/7 15:00
 */
public class MipsLibFunc extends MipsInstruction {
    private String content;
    public MipsLibFunc(String name) {
        switch (name) {
            case "getint" -> content = "li\t$v0,\t5\n" + "\tsyscall\n";
            case "getchar" -> content = "li\t$v0,\t12\n" + "\tsyscall\n";
            case "putint" -> content = "li\t$v0,\t1\n" + "\tsyscall\n";
            case "putch" -> content = "li\t$v0,\t11\n" + "\tsyscall\n";
            case "putstr" -> content = "li\t$v0,\t4\n" + "\tsyscall\n";
        }
    }

    public String toString() {
        return content;
    }
}
