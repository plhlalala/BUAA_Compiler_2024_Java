package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class Character_com {
    public String charConst;

    public static String reverseEscapeSequence(char ch) {
        switch (ch) {
            case '\u0007':
                return "\\a";  // 响铃
            case '\b':
                return "\\b";      // 退格
            case '\t':
                return "\\t";      // 制表符
            case '\n':
                return "\\n";      // 换行
            case '\u000B':
                return "\\v";  // 垂直制表符
            case '\f':
                return "\\f";      // 换页
            case '\r':
                return "\\r";      // 回车
            case '\"':
                return "\\\"";     // 双引号
            case '\'':
                return "\\'";      // 单引号
            case '\\':
                return "\\\\";     // 反斜杠
            case '\0':
                return "\\0";      // 空字符
            default:
                // 如果不是标准转义字符，返回字符本身
                return Character.toString(ch);
        }
    }

    public void analyze(PrintWriter writer) {
        writer.println(LexType.CHRCON.getTypename() + " " + "\'" + reverseEscapeSequence(charConst.charAt(0)) + "\'");
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<Character>";
    }

}
