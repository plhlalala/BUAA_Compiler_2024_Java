package frontend.parser.components.Decl;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.ConstExp;
import frontend.parser.components.Exp.InitVal;

import java.io.PrintWriter;

public class VarDef {
    public String ident;
    public ConstExp constExp;
    public InitVal initVal;
    public boolean hasRBrack;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.IDENFR.getTypename() + " " + ident);
        if (constExp != null) {
            writer.println(LexType.LBRACK);
            constExp.analyze(writer);
            if (hasRBrack) {
                writer.println(LexType.RBRACK);
            }
        }
        if (initVal != null) {
            writer.println(LexType.ASSIGN);
            initVal.analyze(writer);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<VarDef>";
    }
}
