package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class AddExp {
    public MulExp mulExp;
    public LexType op;
    public AddExp addExp;

    public void analyze(PrintWriter writer) {
        if (addExp != null) {
            addExp.analyze(writer);
            writer.println(op);
        }
        mulExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<AddExp>";
    }
}
