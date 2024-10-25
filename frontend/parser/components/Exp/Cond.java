package frontend.parser.components.Exp;

import java.io.PrintWriter;

// Cond → LOrExp
public class Cond {
    public LOrExp lOrExp;

    public void analyze(PrintWriter writer) {
        lOrExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<Cond>";
    }
}
