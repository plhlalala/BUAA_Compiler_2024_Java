package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class InitVal {
    public ArrayList<Exp> exps = new ArrayList<>();
    public String stringConst;
    public boolean hasBrace;

    public void analyze(PrintWriter writer) {
        if (stringConst != null) {
            writer.println(LexType.STRCON.getTypename() + " " + stringConst);
        } else if (exps.size() == 1 && !hasBrace) {
            exps.get(0).analyze(writer);
        } else {
            writer.println(LexType.LBRACE); // {
            for (Exp Exp : exps) {
                if (!exps.get(0).equals(Exp)) {
                    writer.println(LexType.COMMA);
                }
                Exp.analyze(writer);
            }
            writer.println(LexType.RBRACE); // }
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<InitVal>";
    }
}
