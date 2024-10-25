package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.LVal;

import java.io.PrintWriter;

public class GetInt_stmt extends Stmt {
    public LVal lval;
    public boolean hasRightParent;
    public boolean hasSemicolon;
    public int linenum = -1;

    @Override
    public void analyze(PrintWriter writer) {
        lval.analyze(writer);
        writer.println(LexType.ASSIGN);
        writer.println(LexType.GETINTTK);
        writer.println(LexType.LPARENT);
        if (hasRightParent) {
            writer.println(LexType.RPARENT);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }
}
