package frontend.parser.components.Exp;

import java.io.PrintWriter;

public class ConstExp {
    public AddExp addExp;

    public void analyze(PrintWriter writer) {
        addExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<ConstExp>";
    }
}
