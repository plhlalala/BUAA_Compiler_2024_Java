package optimize;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrModule;
import middleend.LLVM_components.IrValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class LiveVariableAnalysis {
    private IrModule module;
    private HashMap<BasicBlock, HashSet<IrValue>> inMap;
    private HashMap<BasicBlock, HashSet<IrValue>> outMap;

    public void optimize(IrModule module) {
        this.module = module;
        for (Function function : module.getFunctionListWithMain()) {
            inMap = new HashMap<>();
            outMap = new HashMap<>();
            for (BasicBlock bb : function.getBasicBlocks()) {
                outMap.put(bb, new HashSet<>());
                inMap.put(bb, new HashSet<>());
            }
            for (BasicBlock bb : function.getBasicBlocks()) {
                bb.buildDefUseChain();
            }
            BuildInOut(function);
        }
    }

    public void BuildInOut(Function func) {
        ArrayList<BasicBlock> blocks = new ArrayList<>(func.getBasicBlocks());
        Collections.reverse(blocks);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock bb : blocks) {
                HashSet<IrValue> newOut = new HashSet<>();
                for (BasicBlock succ : bb.getSuccessors()) {
                    newOut.addAll(inMap.get(succ));
                }
                outMap.put(bb, newOut);
                HashSet<IrValue> newIn = new HashSet<>(newOut);
                newIn.removeAll(bb.getDef());
                newIn.addAll(bb.getUse());
                if (!newIn.equals(inMap.get(bb))) {
                    inMap.put(bb, newIn);
                    changed = true;
                }
            }
        }
        for (BasicBlock block : func.getBasicBlocks()) {
            block.setIn(inMap.get(block));
            block.setOut(outMap.get(block));
        }
    }
}