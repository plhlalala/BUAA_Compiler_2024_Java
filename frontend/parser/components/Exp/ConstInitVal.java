package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ConstInitVal {
    public ArrayList<ConstExp> constExps = new ArrayList<>();
    public String stringConst;
    public boolean hasBrace;

    public void analyze(PrintWriter writer) {
        if (stringConst != null) {
            writer.println(LexType.STRCON.getTypename() + " " + stringConst);
        } else if (constExps.size() == 1 && !hasBrace) {
            constExps.get(0).analyze(writer);
        } else {
            writer.println(LexType.LBRACE); // {
            for (ConstExp constExp : constExps) {
                if (!constExps.get(0).equals(constExp)) {
                    writer.println(LexType.COMMA);
                }
                constExp.analyze(writer);
            }
            writer.println(LexType.RBRACE); // }
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<ConstInitVal>";
    }
}
