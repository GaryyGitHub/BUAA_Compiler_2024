package frontend;

import error.ErrorHandler;
import error.ErrorType;
import error.MyError;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gary
 * @Description: 词法分析器，单例模式
 * @date 2024/9/23 19:52
 */
public class Lexer {
    // 单例模式，用final表示不可修改
    private static final Lexer instance = new Lexer();
    public static Lexer getInstance() {
        return instance;
    }
    // 标识符数组
    private List<Token> tokens = new ArrayList<>();
    // 获取词法分析结果
    public List<Token> getTokens() {
        return tokens;
    }
    // 保留字, 也叫关键字
    private Map<String, TokenType> reservedWords = new HashMap<>();
    private void initReservedWords() {
        reservedWords.put("main", TokenType.MAINTK);
        reservedWords.put("const", TokenType.CONSTTK);
        reservedWords.put("int", TokenType.INTTK);
        reservedWords.put("char", TokenType.CHARTK);
        reservedWords.put("break", TokenType.BREAKTK);
        reservedWords.put("continue", TokenType.CONTINUETK);
        reservedWords.put("if", TokenType.IFTK);
        reservedWords.put("else", TokenType.ELSETK);
        reservedWords.put("for", TokenType.FORTK);
        reservedWords.put("getint", TokenType.GETINTTK);
        reservedWords.put("getchar", TokenType.GETCHARTK);
        reservedWords.put("printf", TokenType.PRINTFTK);
        reservedWords.put("return", TokenType.RETURNTK);
        reservedWords.put("void", TokenType.VOIDTK);
    }

    // 词法分析函数，按照表格顺序: IDENFR -> INTCON -> STRCON ...
    public void analyze(String content) {
        initReservedWords();
//        Boolean result = true;  // 标记词法分析是否成功
        int lineNum = 1;
        int len = content.length();
        // 从开头遍历
        for (int curPos = 0; curPos < len; curPos++) {
            char c = content.charAt(curPos);
            if (c == '\n') lineNum++;
            // 标识符 Ident
            else if (c == '_' || Character.isLetter(c)) {
                String ident = "";
                while (curPos < len && (Character.isLetterOrDigit(content.charAt(curPos)) || content.charAt(curPos) == '_')) {
                    char cc = content.charAt(curPos++);
                    ident += cc;
                }
                curPos--;    // 回退一个字符，因为for循环会自动加1
                // 判断是否是保留字，要不是保留字就是标识符
                TokenType type = reservedWords.get(ident);
                if (type == null) type = TokenType.IDENFR;
                tokens.add(new Token(type, ident, lineNum));
            }
            // 无符号整数 IntConst
            else if (Character.isDigit(c)) {
                String intConst = "";
                while (curPos < len && Character.isDigit(content.charAt(curPos))) {
                    char cc = content.charAt(curPos++);
                    intConst += cc;
                }
                curPos--;    // 回退一个字符，因为for循环会自动加1
                TokenType type = TokenType.INTCON;
                tokens.add(new Token(type, intConst, lineNum));
            }
            // 字符串常量 StringConst
            else if (c == '\"') {
                String strConst = "\"";
                curPos++;
                while (curPos < len && content.charAt(curPos) != '\"') {
                    char cc = content.charAt(curPos++);
                    strConst += cc;
                    if (cc == '\\' && curPos < len && content.charAt(curPos) == '\"') {
                        strConst += content.charAt(curPos);
                        curPos++;    // 跳过转义字符"
                    }
                }
                strConst += "\"";    // 加上结尾的双引号
                TokenType type = TokenType.STRCON;
                tokens.add(new Token(type, strConst, lineNum));
            }
            // 字符常量 CharConst
            else if (c == '\'') {
                String charConst = "'";
                curPos++;
                while (curPos < len) {
                    char cc = content.charAt(curPos++);
                    charConst += cc;
                    if (content.charAt(curPos) == '\'') {
                        if (content.charAt(curPos + 1) != '\'') {
                            break;
                        }
                    }
                }
                charConst += "'";    // 加上结尾的单引号
                TokenType type = TokenType.CHRCON;
                tokens.add(new Token(type, charConst, lineNum));
            }
            // NOT 和 NEQ
            else if (c == '!') {
                if (curPos < len && content.charAt(curPos + 1) == '=') {
                    curPos++;
                    tokens.add(new Token(TokenType.NEQ, "!=", lineNum));
                } else {
                    tokens.add(new Token(TokenType.NOT, "!", lineNum));
                }
            }
            // AND &&
            else if (c == '&') {
                if (curPos < len && content.charAt(curPos + 1) == '&') {
                    curPos++;
                    tokens.add(new Token(TokenType.AND, "&&", lineNum));
                } else {
                    ErrorHandler.getInstance().addErrorTable(new MyError(lineNum, ErrorType.a));
                    tokens.add(new Token(TokenType.AND, "&&", lineNum));
//                    result = false;
                }
            }
            // OR ||
            else if (c == '|') {
                if (curPos < len && content.charAt(curPos + 1) == '|') {
                    curPos++;
                    tokens.add(new Token(TokenType.OR, "||", lineNum));
                } else {
                    ErrorHandler.getInstance().addErrorTable(new MyError(lineNum, ErrorType.a));
                    tokens.add(new Token(TokenType.AND, "&&", lineNum));
//                    result = false;
                }
            }
            // PLUS +
            else if (c == '+') tokens.add(new Token(TokenType.PLUS, "+", lineNum));
            // MINUS -
            else if (c == '-') tokens.add(new Token(TokenType.MINU, "-", lineNum));
            // MULTI *
            else if (c == '*') tokens.add(new Token(TokenType.MULT, "*", lineNum));
            // DIV / 需要额外考虑注释的情况
            else if (c == '/') {
                String token = "/";
                // 单行注释
                if (curPos + 1 < len && content.charAt(curPos + 1) == '/') {
                    curPos++;
                    while (curPos < len && content.charAt(curPos) != '\n') {
                        token += content.charAt(curPos++);
                    }
                    // 此时只可能是换行符或文件结尾！
                    if (curPos < len) {
                        token += content.charAt(curPos++);
                        lineNum++;
                    }
                    curPos--;
//                    System.out.println("单行注释：" + token);
                }
                // 多行注释
                else if (curPos + 1 < len && content.charAt(curPos + 1) == '*') {
                    curPos++;
                    while (curPos < len) {  // 状态转换循环
                        while (curPos < len && content.charAt(curPos) != '*') {
                            // 非*字符，继续循环
                            char cc = content.charAt(curPos++);
                            token += cc;
                            if (cc == '\n') lineNum++;
                        }
                        while (curPos < len && content.charAt(curPos) == '*') {
                            // 遇到*，继续循环。如果不是*，则继续走大循环，跳到上一个小循环
                            token += content.charAt(curPos++);
                        }
                        if (curPos < len && content.charAt(curPos) == '/') {
                            // 找到*/，结束注释
                            token += content.charAt(curPos);
                            break;
                        }
                    }
//                    System.out.println("多行注释：" + token);
                }
                // 除法运算符
                else {
                    tokens.add(new Token(TokenType.DIV, "/", lineNum));
                }
            }
            else if (c == '%') tokens.add(new Token(TokenType.MOD, "%", lineNum));
            // LSS > LEQ >=
            else if (c == '<') {
                if (curPos < len && content.charAt(curPos + 1) == '=') {
                    curPos++;
                    tokens.add(new Token(TokenType.LEQ, "<=", lineNum));
                } else {
                    tokens.add(new Token(TokenType.LSS, "<", lineNum));
                }
            }
            // GRE > GEQ >=
            else if (c == '>') {
                if (curPos < len && content.charAt(curPos + 1) == '=') {
                    curPos++;
                    tokens.add(new Token(TokenType.GEQ, ">=", lineNum));
                } else {
                    tokens.add(new Token(TokenType.GRE, ">", lineNum));
                }
            }
            // ASSIGN = EQL ==
            else if (c == '=') {
                if (curPos < len && content.charAt(curPos + 1) == '=') {
                    curPos++;
                    tokens.add(new Token(TokenType.EQL, "==", lineNum));
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", lineNum));
                }
            }
            // SEMICN ;
            else if (c == ';') tokens.add(new Token(TokenType.SEMICN, ";", lineNum));
            // COMMA ,
            else if (c == ',') tokens.add(new Token(TokenType.COMMA, ",", lineNum));
            // LPARENT (
            else if (c == '(') tokens.add(new Token(TokenType.LPARENT, "(", lineNum));
            // RPARENT )
            else if (c == ')') tokens.add(new Token(TokenType.RPARENT, ")", lineNum));
            // LBRACK [
            else if (c == '[') tokens.add(new Token(TokenType.LBRACK, "[", lineNum));
            // RBRACK ]
            else if (c == ']') tokens.add(new Token(TokenType.RBRACK, "]", lineNum));
            // LBRACE {
            else if (c == '{') tokens.add(new Token(TokenType.LBRACE, "{", lineNum));
            // RBRACE }
            else if (c == '}') tokens.add(new Token(TokenType.RBRACE, "}", lineNum));
        }
//        return result;
    }

    // 向指定输出文件输出结果
    public void printResult() {
        for (Token token : tokens) {
            IOUtils.write(token.toString());
            System.out.print(token);
        }
    }
}
