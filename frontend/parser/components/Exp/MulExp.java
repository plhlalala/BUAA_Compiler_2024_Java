package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class MulExp {
    public UnaryExp unaryExp;
    public LexType op; //  * or / or %
    public MulExp mulExp;

    public void analyze(PrintWriter writer) {
        if (mulExp != null) {
            mulExp.analyze(writer);
            writer.println(op);
        }
        unaryExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<MulExp>";
    }
}
