package frontend.parser.components.Stmt;

import frontend.lexer.LexType;
import frontend.parser.components.Exp.Exp;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Print_stmt extends Stmt {
    public String stringConst;
    public ArrayList<Exp> exps = new ArrayList<>();
    public boolean hasRightParen;
    public boolean hasSemicolon;
    public int linenum = -1;

    @Override
    public void analyze(PrintWriter writer) {
        writer.println(LexType.PRINTFTK);
        writer.println(LexType.LPARENT);
        assert stringConst != null;
        writer.println(LexType.STRCON.getTypename() + " " + stringConst);
        for (Exp exp : exps) {
            writer.println(LexType.COMMA);
            exp.analyze(writer);
        }
        if (hasRightParen) {
            writer.println(LexType.RPARENT);
        }
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }
}