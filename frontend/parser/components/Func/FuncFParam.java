package frontend.parser.components.Func;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.BType;

import java.io.PrintWriter;


// FuncFParam → BType Ident ['[' ']']
public class FuncFParam {
    public BType bType;
    public String ident;
    public boolean isArray;
    public boolean hasRightBracket;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        bType.analyze(writer);
        writer.println(LexType.IDENFR.getTypename() + " " + ident);
        if (isArray) {
            writer.println(LexType.LBRACK);
            if (hasRightBracket) {
                writer.println(LexType.RBRACK);
            }
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<FuncFParam>";
    }
}
