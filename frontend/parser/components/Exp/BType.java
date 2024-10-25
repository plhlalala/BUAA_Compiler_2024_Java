package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class BType {
    public LexType type;

    public void analyze(PrintWriter writer) {
        assert type == LexType.INTTK || type == LexType.CHARTK;
        writer.println(type);
    }

    @Override
    public String toString() {
        return "<BType>";
    }
}
