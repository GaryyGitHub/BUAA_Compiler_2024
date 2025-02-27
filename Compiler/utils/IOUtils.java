package utils;

import java.io.*;

/**
 * @author Gary
 * @Description: 输入输出相关函数
 * @date 2024/9/24 9:16
 */
public class IOUtils {
    public static String inputFile = "testfile.txt";
    public static String llvmFile = "llvm_ir.txt";
    public static String outputFile = "mips.txt";
    public static String errorFile = "error.txt";
    public static String read(String filePath) throws IOException {
        StringBuilder code = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            code.append(line).append("\n");
        }
        return code.toString();
    }

    public static void write(String content, String filePath)  {
        // 不加true会覆盖原文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String content) {
        write(content, outputFile);
    }

    public static void clean(String filePath) throws IOException {
        File f = new File(filePath);
        FileWriter fw = new FileWriter(f);
        fw.write("");
        fw.flush();
        fw.close();
    }
}
