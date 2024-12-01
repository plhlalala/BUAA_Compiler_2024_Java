package frontend.lexer;

import frontend.error.ErrorRecord;
import frontend.error.ErrorType;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private static final HashMap<String, LexType> reservedWords = new HashMap<>() {{
        put("main", LexType.MAINTK);
        put("const", LexType.CONSTTK);
        put("int", LexType.INTTK);
        put("char", LexType.CHARTK);
        put("break", LexType.BREAKTK);
        put("continue", LexType.CONTINUETK);
        put("if", LexType.IFTK);
        put("else", LexType.ELSETK);
        put("for", LexType.FORTK);
        put("getint", LexType.GETINTTK);
        put("getchar", LexType.GETCHARTK);
        put("printf", LexType.PRINTFTK);
        put("return", LexType.RETURNTK);
        put("void", LexType.VOIDTK);
    }};
    private static final HashMap<String, LexType> doubleCharOperators = new HashMap<>() {{
        put("&&", LexType.AND);
        put("||", LexType.OR);
        put("<=", LexType.LEQ);
        put(">=", LexType.GEQ);
        put("==", LexType.EQL);
        put("!=", LexType.NEQ);
    }};
    private static final HashMap<Character, LexType> singleCharTokens = new HashMap<>() {{
        put('+', LexType.PLUS);
        put('-', LexType.MINU);
        put('*', LexType.MULT);
        put('/', LexType.DIV);
        put('%', LexType.MOD);
        put('<', LexType.LSS);
        put('>', LexType.GRE);
        put('=', LexType.ASSIGN);
        put(';', LexType.SEMICN);
        put(',', LexType.COMMA);
        put('(', LexType.LPARENT);
        put(')', LexType.RPARENT);
        put('[', LexType.LBRACK);
        put(']', LexType.RBRACK);
        put('{', LexType.LBRACE);
        put('}', LexType.RBRACE);
        put('!', LexType.NOT);
    }};
    private final PushbackReader reader;
    private final ArrayList<ErrorRecord> errorRecords;
    private int lineNum;
    private Token token;

    public Lexer(PushbackReader reader, ArrayList<ErrorRecord> errorRecords) {
        this.reader = reader;
        this.errorRecords = errorRecords;
        this.lineNum = 1;
    }

    public static char processEscapeSequence(char ch) {
        return switch (ch) {
            case 'a' -> '\u0007';  // 响铃
            case 'b' -> '\b';      // 退格
            case 't' -> '\t';      // 制表符
            case 'n' -> '\n';      // 换行
            case 'v' -> '\u000B';  // 垂直制表符
            case 'f' -> '\f';      // 换页
            case '\"' -> '\"';     // 双引号
            case '\'' -> '\'';     // 单引号
            case '\\' -> '\\';     // 反斜杠
            case '0' -> '\0';      // 空字符
            default -> {
                System.out.println("Error: Invalid escape sequence");
                yield ch;
            }
        };
    }

    public boolean next() throws IOException {
        String value;
        LexType type;
        StringBuilder sb = new StringBuilder();
        int ch = reader.read();
        while (Character.isWhitespace(ch)) {
            if (ch == '\n') {
                lineNum++;
            }
            ch = reader.read();
            if (ch == -1) {
                return false;
            }
        }
        if (ch == -1) {
            return false;
        }

        if (ch == '/') {
            int nextChar = reader.read();
            if (nextChar == '/') {
                while (ch != '\n') {
                    ch = reader.read();
                    if (ch == -1) {
                        return false;
                    }
                }
                lineNum++;
                return next();
            } else if (nextChar == '*') {
                while (true) {
                    ch = reader.read();
                    if (ch == '\n') {
                        lineNum++;
                    }
                    if (ch == '*') {
                        int next = reader.read();
                        if (next == '/') {
                            return next();
                        } else {
                            reader.unread(next);
                        }
                    }
                }
            } else {
                reader.unread(nextChar);
            }
        }
        sb.append((char) ch);
        if (Character.isLetter(ch) || ch == '_') {
            ch = reader.read();
            while (Character.isLetterOrDigit(ch) || ch == '_') {
                sb.append((char) ch);
                ch = reader.read();
            }
            if (ch != -1) {
                reader.unread(ch);
            }
            value = sb.toString();
            type = reservedWords.getOrDefault(value, LexType.IDENFR);
            token = new Token(value, type, lineNum);
            return true;
        } else if (Character.isDigit(ch)) {
            if (ch == '0') {
                value = "0";
            } else {
                ch = reader.read();
                while (Character.isDigit(ch)) {
                    sb.append((char) ch);
                    ch = reader.read();
                }
                if (ch != -1) {
                    reader.unread(ch);
                }
                value = sb.toString();
            }
            token = new Token(value, LexType.INTCON, lineNum);
            return true;
        } else if (ch == '\'') {
            sb = new StringBuilder();
            ch = reader.read();
            if (ch == '\\') {  // 处理转义字符
                ch = reader.read();
                ch = processEscapeSequence((char) ch);
            }
            sb.append((char) ch);
            ch = reader.read();
            if (ch == '\'') {
                value = sb.toString();
                token = new Token(value, LexType.CHRCON, lineNum);
                return true;
            }
        } else if (ch == '\"') {
            sb = new StringBuilder();
            ch = reader.read();
            while (ch != '\"') {
                if (ch == '\\') {
                    ch = reader.read();
                    ch = processEscapeSequence((char) ch);
                }
                sb.append((char) ch);
                ch = reader.read();
            }
            value = sb.toString();
            token = new Token(value, LexType.STRCON, lineNum);
            return true;
        }
        if (ch == '>' || ch == '<' || ch == '&' || ch == '|' || ch == '=' || ch == '!') {
            int nextChar = reader.read();
            if (nextChar != -1) {
                sb.append((char) nextChar);
                String doubleChar = sb.toString();
                if (doubleCharOperators.containsKey(doubleChar)) {
                    token = new Token(doubleChar, doubleCharOperators.get(doubleChar), lineNum);
                    return true;
                } else if (ch == '&' || ch == '|') {
                    reader.unread(nextChar);
                    addErrorRecord(ErrorType.ILLEGAL_SYMBOL, lineNum);
                    String right = (char) ch + String.valueOf((char) ch);
                    token = new Token(doubleChar.substring(0, 1), doubleCharOperators.get(right), lineNum);
                    return true;
                }
                reader.unread(nextChar);
            }
        }
        value = sb.substring(0, 1);
        type = singleCharTokens.get(value.charAt(0));
        token = new Token(value, type, lineNum);
        // System.out.println("lexer：" + token + " " + lineNum + " " + value);
        return true;
    }

    public Token getToken() {
        return this.token;
    }

    public void addErrorRecord(ErrorType errorType, int lineNumber) {
        errorRecords.add(new ErrorRecord(errorType, lineNumber));
    }
}
