package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Exp;
import frontend.parser.components.Exp.LVal;

import java.io.PrintWriter;

// LVal '=' Exp ';'
public class AssignLval_stmt extends Stmt {
    public LVal lval;
    public Exp exp;
    public boolean hasSemicolon;
    public int linenum = -1;

    @Override
    public void analyze(PrintWriter writer) {
        lval.analyze(writer);
        writer.println(LexType.ASSIGN);
        exp.analyze(writer);
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }
}
