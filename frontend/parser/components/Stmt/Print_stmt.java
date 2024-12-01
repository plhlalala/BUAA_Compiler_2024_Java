package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Exp;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Print_stmt extends Stmt {
    public String stringConst;
    public ArrayList<Exp> exps = new ArrayList<>();
    public boolean hasRightParen;
    public boolean hasSemicolon;
    public int linenum = -1;

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

    @Override
    public void analyze(PrintWriter writer) {
        writer.println(LexType.PRINTFTK);
        writer.println(LexType.LPARENT);
        assert stringConst != null;
        writer.println(LexType.STRCON.getTypename() + " " + "\"" + reverseEscapeSequence(stringConst) + "\"");
        for (Exp exp : exps) {
            writer.println(LexType.COMMA);
            exp.analyze(writer);
        }
        if (hasRightParen) {
            writer.println(LexType.RPARENT);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }
}