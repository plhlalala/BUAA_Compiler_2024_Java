package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class LAndExp {
    public EqExp eqExp;
    public LAndExp lAndExp;
    public String op; // 可能缺少符号

    public void analyze(PrintWriter writer) {
        if (lAndExp != null) {
            lAndExp.analyze(writer);
            writer.println(LexType.AND.getTypename() + " " + op);
        }
        eqExp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<LAndExp>";
    }
}
