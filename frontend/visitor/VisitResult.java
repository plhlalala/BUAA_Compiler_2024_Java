package frontend.visitor;

import frontend.symtable.VarType;
import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;

import java.util.ArrayList;


public class VisitResult {
    public VarType varType = new VarType();
    public ArrayList<VarType> paraTypeList = new ArrayList<>();

    public Integer constInt;
    public ArrayList<Integer> integerList = new ArrayList<>();

    public Value irValue;
    public ArrayList<Value> irValueList = new ArrayList<>();

    public boolean hasReturnInLastSentence = false;
    public ArrayList<Integer> returnNotVoidLineNumber = new ArrayList<>();

    public ArrayList<BasicBlock> ifTrueNeedToJumpOut = new ArrayList<>();
    public ArrayList<BasicBlock> ifFalseNeedToJumpNext = new ArrayList<>();
    public ArrayList<BasicBlock> andBlocks = new ArrayList<>();
    public ArrayList<BasicBlock> waitingAndBlocks = new ArrayList<>();

    public BasicBlock getLastAndBlock() {
        return andBlocks.get(andBlocks.size() - 1);
    }
}
