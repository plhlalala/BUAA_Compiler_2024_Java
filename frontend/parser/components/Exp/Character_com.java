package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class Character_com {
    public String charConst;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.CHRCON.getTypename() + " " + charConst);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<Character>";
    }
}
