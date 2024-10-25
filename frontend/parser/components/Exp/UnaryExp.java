package frontend.parser.components.Exp;

import frontend.lexer.LexType;

import java.io.PrintWriter;

public class UnaryExp {
    public PrimaryExp primaryExp;
    public String ident;
    public FuncRParams funcRParams;
    public boolean hasRightParent;
    public UnaryExp unaryExp;
    public UnaryOp op;
    public int linenum = -1;

    public void analyze(PrintWriter writer) {
        if (unaryExp != null) {
            op.analyze(writer);
            unaryExp.analyze(writer);
        } else if (ident != null) {
            writer.println(LexType.IDENFR.getTypename() + " " + ident);
            writer.println(LexType.LPARENT);
            if (funcRParams != null) {
                funcRParams.analyze(writer);
            }
            if (hasRightParent) {
                writer.println(LexType.RPARENT);
            }
        } else {
            primaryExp.analyze(writer);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<UnaryExp>";
    }
}
