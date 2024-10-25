package frontend.parser.components.Stmt;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class Break_Continue_stmt extends Stmt {
    public LexType type;
    public boolean hasSemicolon;
    public int linenum = -1;

    @Override
    public void analyze(PrintWriter writer) {
        writer.println(type);
        if (hasSemicolon) {
            writer.println(LexType.SEMICN);
        }
        writer.println(this);
    }
}
