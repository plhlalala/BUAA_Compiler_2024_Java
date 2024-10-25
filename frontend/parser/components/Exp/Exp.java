package frontend.parser.components.Exp;

import java.io.PrintWriter;

public class Exp {
    public AddExp addExp;

    public void analyze(PrintWriter writer) {
        addExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<Exp>";
    }
}
