package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class LVal {
    public String ident;
    public Exp exp;
    public boolean hasRightBracket;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.IDENFR.getTypename() + " " + ident);
        if (exp != null) {
            writer.println(LexType.LBRACK);
            exp.analyze(writer);
            if (hasRightBracket) {
                writer.println(LexType.RBRACK);
            }
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<LVal>";
    }
}
