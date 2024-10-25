package frontend.parser.components.Decl;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.BType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class VarDecl {
    public BType bType; // int, char
    public ArrayList<VarDef> varDefs = new ArrayList<>();
    public boolean hasSemicolon;

    public void analyze(PrintWriter writer) {
        bType.analyze(writer);
        for (VarDef varDef : varDefs) {
            if (!varDefs.get(0).equals(varDef)) {
                writer.println(LexType.COMMA);
            }
            varDef.analyze(writer);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<VarDecl>";
    }
}
