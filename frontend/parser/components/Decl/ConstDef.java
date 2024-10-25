package frontend.parser.components.Decl;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.ConstExp;
import frontend.parser.components.Exp.ConstInitVal;

import java.io.PrintWriter;

public class ConstDef {
    public String ident;
    public ConstExp constExp;
    public boolean hasRightBrack;
    public ConstInitVal constInitVal;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.IDENFR.getTypename() + " " + ident);
        if (constExp != null) {
            writer.println(LexType.LBRACK);
            constExp.analyze(writer);
            if (hasRightBrack) {
                writer.println(LexType.RBRACK);
            }
        }
        writer.println(LexType.ASSIGN);
        constInitVal.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<ConstDef>";
    }
}
