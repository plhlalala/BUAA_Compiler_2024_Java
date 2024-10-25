package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Cond;

import java.io.PrintWriter;

// 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
public class For_stmt extends Stmt {
    public ForStmt fotStmt1;
    public ForStmt forStmt2;
    public Cond cond;
    public Stmt stmt;

    @Override
    public void analyze(PrintWriter writer) {
        writer.println(LexType.FORTK);
        writer.println(LexType.LPARENT);
        if (fotStmt1 != null) {
            fotStmt1.analyze(writer);
        }
        writer.println(LexType.SEMICN);
        if (cond != null) {
            cond.analyze(writer);
        }
        writer.println(LexType.SEMICN);
        if (forStmt2 != null) {
            forStmt2.analyze(writer);
        }
        writer.println(LexType.RPARENT);
        stmt.analyze(writer);
        writer.println(this);
    }
}
