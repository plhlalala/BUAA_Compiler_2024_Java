package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class LOrExp {
    public LOrExp lOrExp;
    public LAndExp lAndExp;
    public String op; // 可能缺少符号

    public void analyze(PrintWriter writer) {
        if (lOrExp != null) {
            lOrExp.analyze(writer);
            writer.println(LexType.OR.getTypename() + " " + op);
        }
        lAndExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<LOrExp>";
    }
}
