package frontend.parser.components.Stmt;

import frontend.parser.components.Block;

import java.io.PrintWriter;

public class Block_stmt extends Stmt {
    public Block block;

    @Override
    public void analyze(PrintWriter writer) {
        block.analyze(writer);
        writer.println(this);
    }
}
