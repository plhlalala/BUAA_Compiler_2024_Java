package frontend.visitor;

import frontend.symtable.VarType;

import java.util.ArrayList;


public class VisitResult {
    public static final int magicNum = 2004082052;
    public VarType varType = new VarType();
    public ArrayList<VarType> paraTypeList = new ArrayList<>();
    public int value = magicNum;
    public ArrayList<Integer> valueList;
    public boolean hasReturnInLastSentence = false;
    public ArrayList<Integer> returnNotVoidLineNumber = new ArrayList<>();
}
