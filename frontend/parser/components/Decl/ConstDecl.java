package frontend.parser.components.Decl;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.BType;

import java.io.PrintWriter;
import java.util.ArrayList;

// ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDecl {
    public BType type;
    public ArrayList<ConstDef> constDefs = new ArrayList<>();
    public boolean hasSemicolon;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.CONSTTK);
        type.analyze(writer);
        for (ConstDef constDef : constDefs) {
            if (!constDefs.get(0).equals(constDef)) {
                writer.println(LexType.COMMA);
            }
            constDef.analyze(writer);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<ConstDecl>";
    }
}