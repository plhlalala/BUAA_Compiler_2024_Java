package frontend.parser.components;

import frontend.lexer.LexType;

import java.io.PrintWriter;
import java.util.ArrayList;

//  Block → '{' { BlockItem } '}'
public class Block {
    public ArrayList<BlockItem> blockItems = new ArrayList<>();

    public void analyze(PrintWriter writer) {
        writer.println(LexType.LBRACE);
        for (BlockItem blockItem : blockItems) {
            blockItem.analyze(writer);
        }
        writer.println(LexType.RBRACE);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<Block>";
    }
}
