package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class PrimaryExp {
    public Exp exp;
    public boolean hasRightParent;
    public LVal lval;
    public Number_com number;
    public Character_com character;

    public void analyze(PrintWriter writer) {
        if (exp != null) {
            writer.println(LexType.LPARENT);
            exp.analyze(writer);
            if (hasRightParent) {
                writer.println(LexType.RPARENT);
            }
        } else if (lval != null) {
            lval.analyze(writer);
        } else if (number != null) {
            number.analyze(writer);
        } else {
            character.analyze(writer);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<PrimaryExp>";
    }
}
