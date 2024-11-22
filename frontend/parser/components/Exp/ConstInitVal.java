package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ConstInitVal {
    public ArrayList<ConstExp> constExps = new ArrayList<>();
    public String stringConst;
    public boolean hasBrace;

    public void analyze(PrintWriter writer) {
        if (stringConst != null) {
            writer.println(LexType.STRCON.getTypename() + " " + "\"" + reverseEscapeSequence(stringConst) + "\"");
        } else if (constExps.size() == 1 && !hasBrace) {
            constExps.get(0).analyze(writer);
        } else {
            writer.println(LexType.LBRACE); // {
            for (ConstExp constExp : constExps) {
                if (!constExps.get(0).equals(constExp)) {
                    writer.println(LexType.COMMA);
                }
                constExp.analyze(writer);
            }
            writer.println(LexType.RBRACE); // }
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<ConstInitVal>";
    }

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

    public static String reverseEscapeSequence(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            sb.append(reverseEscapeSequence(ch));
        }
        return sb.toString();
    }
}
