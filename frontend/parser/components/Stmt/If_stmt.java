package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Cond;

import java.io.PrintWriter;

public class If_stmt extends Stmt {
    public Cond cond;
    public Stmt stmt;
    public Stmt elseStmt;
    public boolean hasRightParent;

    @Override
    public void analyze(PrintWriter writer) {
        writer.println(LexType.IFTK);
        writer.println(LexType.LPARENT);
        cond.analyze(writer);
        if (hasRightParent) {
            writer.println(LexType.RPARENT);
        }
        stmt.analyze(writer);
        if (elseStmt != null) {
            writer.println(LexType.ELSETK);
            elseStmt.analyze(writer);
        }
        writer.println(this);
    }
}
