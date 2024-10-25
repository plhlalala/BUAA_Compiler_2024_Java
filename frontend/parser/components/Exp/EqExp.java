package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class EqExp {
    public EqExp eqExp;
    public RelExp relExp;
    public LexType type; // == or !=

    public void analyze(PrintWriter writer) {
        if (eqExp != null) {
            eqExp.analyze(writer);
            writer.println(type);
        }
        relExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<EqExp>";
    }

}
