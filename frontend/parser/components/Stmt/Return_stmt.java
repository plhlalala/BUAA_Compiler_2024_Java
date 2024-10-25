package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Exp;

import java.io.PrintWriter;

public class Return_stmt extends Stmt {
    public Exp exp;
    public boolean hasSemicolon;
    public int linenum = -1;

    @Override
    public void analyze(PrintWriter writer) {
        writer.println(LexType.RETURNTK);
        if (exp != null) {
            exp.analyze(writer);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }

}
