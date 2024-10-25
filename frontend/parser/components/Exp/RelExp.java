package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class RelExp {
    public AddExp addExp;
    public RelExp relExp;
    public LexType type; // <, <=, >, >=

    public void analyze(PrintWriter writer) {
        if (relExp != null) {
            relExp.analyze(writer);
            assert type == LexType.LSS || type == LexType.LEQ || type == LexType.GRE || type == LexType.GEQ;
            writer.println(type);
        }
        addExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<RelExp>";
    }
}
