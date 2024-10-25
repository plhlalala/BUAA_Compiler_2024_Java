package frontend.parser.components.Decl;

import java.io.PrintWriter;

public class Decl {
    public ConstDecl constDecl;
    public VarDecl varDecl;

    public void analyze(PrintWriter writer) {
        if (constDecl != null) {
            constDecl.analyze(writer);
        } else if (varDecl != null) {
            varDecl.analyze(writer);
        }
    }

    @Override
    public String toString() {
        return "<Decl>";
    }
}
