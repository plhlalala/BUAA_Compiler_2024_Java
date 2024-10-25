package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class UnaryOp {
    public LexType type;

    public void analyze(PrintWriter writer) {
        writer.println(type);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<UnaryOp>";
    }
}
