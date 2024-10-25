package frontend.lexer;

public class Token {
    private final String value;
    private final LexType type;
    private final int lineNum;

    public Token(String value, LexType type, int lineNum) {
        this.value = value;
        this.type = type;
        this.lineNum = lineNum;
    }

    public String getValue() {
        return this.value;
    }

    public LexType getType() {
        return this.type;
    }

    public boolean isMatch(LexType type) {
        return this.type == type;
    }

    public boolean isNotMatch(LexType type) {
        return this.type != type;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public boolean isBtype() {
        return this.type == LexType.INTTK || this.type == LexType.CHARTK;
    }

    public boolean isFuncType() {
        return this.type == LexType.VOIDTK || isBtype();
    }

    @Override
    public String toString() {
        return "Token{value='" + value + "', type=" + type + ", lineNum=" + lineNum + "}";
    }
}
