package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class Number_com {
    public String intConst;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.INTCON.getTypename() + " " + intConst);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<Number>";
    }
}
