package frontend.parser.components.Func;

import frontend.lexer.LexType;
import frontend.parser.components.Block;

import java.io.PrintWriter;

// MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDef {
    public Block block;
    public boolean hasRightParen;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        writer.println(LexType.INTTK);
        writer.println(LexType.MAINTK);
        writer.println(LexType.LPARENT);
        if (hasRightParen) {
            writer.println(LexType.RPARENT);
        }
        block.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<MainFuncDef>";
    }
}
