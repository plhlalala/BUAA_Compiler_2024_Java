package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Exp;
import frontend.parser.components.Exp.LVal;

import java.io.PrintWriter;

// ForStmt → LVal '=' Exp
public class ForStmt {
    public LVal lval;
    public Exp exp;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        lval.analyze(writer);
        writer.println(LexType.ASSIGN);
        exp.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<ForStmt>";
    }
}
