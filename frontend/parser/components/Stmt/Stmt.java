package frontend.parser.components.Stmt;

import java.io.PrintWriter;

public abstract class Stmt {
    public void analyze(PrintWriter writer) {

    }

    @Override
    public String toString() {
        return "<Stmt>";
    }
}