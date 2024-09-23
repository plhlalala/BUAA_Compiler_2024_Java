package frontend;

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

    public int getLineNum() {
        return this.lineNum;
    }

    @Override
    public String toString() {
        return "Token{value='" + value + "', type=" + type + ", lineNum=" + lineNum + "}";
    }
}
