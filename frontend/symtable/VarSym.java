package frontend.symtable;

import frontend.lexer.LexType;

import java.util.ArrayList;

public class VarSym extends Symbol {
    public boolean isConst = false;
    public VarType varType;
    public int arraySize;
    public ArrayList<Integer> valueList = new ArrayList<>();

    public VarSym(String ident, VarType type) {
        this.ident = ident;
        this.varType = type;
    }

    public String toString() {
        if (isConst) {
            if (varType.isArray) {
                if (varType.type == LexType.INTTK) {
                    return this.ident + " ConstIntArray";
                } else {
                    return this.ident + " ConstCharArray";
                }
            } else {
                if (varType.type == LexType.INTTK) {
                    return this.ident + " ConstInt";
                } else {
                    return this.ident + " ConstChar";
                }
            }
        } else {
            if (varType.isArray) {
                if (varType.type == LexType.INTTK) {
                    return this.ident + " IntArray";
                } else {
                    return this.ident + " CharArray";
                }
            } else {
                if (varType.type == LexType.INTTK) {
                    return this.ident + " Int";
                } else {
                    return this.ident + " Char";
                }
            }
        }
    }
}
