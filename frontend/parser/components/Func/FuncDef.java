package frontend.parser.components.Func;

import frontend.lexer.LexType;
import frontend.parser.components.Block;

import java.io.PrintWriter;

//函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
//函数实参表 FuncRParams → Exp { ',' Exp }
// FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
public class FuncDef {
    public FuncType funcType;
    public String ident;
    public FuncFParams funcFParams; // 无函数参数为NULL
    public Block block;
    public boolean hasRightParen;
    public int identLinenum = -1;
    public int blockLinenum = -1;

    public void analyze(PrintWriter writer) {
        funcType.analyze(writer);
        writer.println(LexType.IDENFR.getTypename() + " " + ident);
        writer.println(LexType.LPARENT);
        if (funcFParams != null) {
            funcFParams.analyze(writer);
        }
        if (hasRightParen) {
            writer.println(LexType.RPARENT);
        }
        block.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<FuncDef>";
    }
}
