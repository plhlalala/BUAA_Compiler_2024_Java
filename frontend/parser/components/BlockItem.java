package frontend.parser.components;

import frontend.parser.components.Decl.Decl;
import frontend.parser.components.Stmt.Stmt;

import java.io.PrintWriter;

// BlockItem → Decl | Stmt
public class BlockItem {
    public Decl decl;
    public Stmt stmt;

    public void analyze(PrintWriter writer) {
        if (decl != null) {
            decl.analyze(writer);
        } else if (stmt != null) {
            stmt.analyze(writer);
        }
    }

    @Override
    public String toString() {
        return "<BlockItem>";
    }
}
