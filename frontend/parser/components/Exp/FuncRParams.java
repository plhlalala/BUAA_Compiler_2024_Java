package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;
import java.util.ArrayList;

//  FuncRParams → Exp { ',' Exp }
public class FuncRParams {
    public ArrayList<Exp> exps = new ArrayList<>();

    public void analyze(PrintWriter writer) {
        for (Exp exp : exps) {
            if (!exps.get(0).equals(exp)) {
                writer.println(LexType.COMMA);
            }
            exp.analyze(writer);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<FuncRParams>";
    }
}
