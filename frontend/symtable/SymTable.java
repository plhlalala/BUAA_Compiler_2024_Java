package frontend.symtable;

import java.util.ArrayList;
import java.util.HashMap;

public class SymTable {
    public static int count = 1;
    public SymTable parent;
    public HashMap<String, Symbol> table = new HashMap<>();
    public ArrayList<Symbol> symbolList = new ArrayList<>();
    public ArrayList<SymTable> children = new ArrayList<>();
    public int id = count++;

    public boolean add(Symbol symbol) {
        if (table.containsKey(symbol.ident)) {
            return false;
        }
        table.put(symbol.ident, symbol);
        symbolList.add(symbol);
        return true;
    }

    public boolean contain(String ident) {
        return table.containsKey(ident);
    }

    public Symbol get(String ident) {
        if (table.containsKey(ident)) {
            return table.get(ident);
        }
        if (parent != null) {
            return parent.get(ident);
        }
        return null;
    }

    public SymTable createChild() {
        SymTable child = new SymTable();
        child.parent = this;
        this.children.add(child);
        return child;
    }

    public SymTable getParent() {
        return parent;
    }
}
