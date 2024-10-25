package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Exp;

import java.io.PrintWriter;

// [Exp] ';'
public class Exp_stmt extends Stmt {
    public Exp exp;
    public boolean hasSemicolon;

    public void analyze(PrintWriter writer) {
        if (exp != null) {
            exp.analyze(writer);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }
}
