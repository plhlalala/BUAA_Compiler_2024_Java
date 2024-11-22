package middleend.LLVM_components;

import middleend.instruction.AllocaInstr;
import middleend.instruction.BinaryInstr;
import middleend.instruction.BinaryOp;
import middleend.instruction.BrInstr;
import middleend.instruction.CallInstr;
import middleend.instruction.GetelementptrInstr;
import middleend.instruction.IcmpCondEnum;
import middleend.instruction.IcmpInstr;
import middleend.instruction.Instruction;
import middleend.instruction.LoadInstr;
import middleend.instruction.ReturnInstr;
import middleend.instruction.StoreInstr;
import middleend.instruction.TruncInstr;
import middleend.instruction.ZextInstr;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class BasicBlock extends Value {
    private Function parentFunction;
    private ArrayList<Instruction> instructions;
    private int loopNum;

    public BasicBlock(Function parentFunction) {
        super(new BasicType(BaseTypeEnum.LABEL, 0));
        this.parentFunction = parentFunction;
        this.instructions = new ArrayList<>();
        loopNum = 0;
    }

    public Value createAddInstr(Value left, Value right) {
        Instruction addInstr = new BinaryInstr(BinaryOp.ADD, left, right, this);
        this.instructions.add(addInstr);
        addInstr.setParentBasicBlock(this);
        return addInstr;
    }

    public Value createSubInstr(Value left, Value right) {
        Instruction subInstr = new BinaryInstr(BinaryOp.SUB, left, right, this);
        this.instructions.add(subInstr);
        subInstr.setParentBasicBlock(this);
        return subInstr;
    }

    public Value createMulInstr(Value left, Value right) {
        Instruction mulInstr = new BinaryInstr(BinaryOp.MUL, left, right, this);
        this.instructions.add(mulInstr);
        mulInstr.setParentBasicBlock(this);
        return mulInstr;
    }

    public Value createSDivInstr(Value left, Value right) {
        Instruction divInstr = new BinaryInstr(BinaryOp.SDIV, left, right, this);
        this.instructions.add(divInstr);
        divInstr.setParentBasicBlock(this);
        return divInstr;
    }

    public Value createSRemInstr(Value left, Value right) {
        Instruction remInstr = new BinaryInstr(BinaryOp.SREM, left, right, this);
        this.instructions.add(remInstr);
        remInstr.setParentBasicBlock(this);
        return remInstr;
    }

    public Value createICmpInstr(IcmpCondEnum cond, Value left, Value right) {
        Instruction icmpInstr = new IcmpInstr(cond, left, right, this);
        this.instructions.add(icmpInstr);
        icmpInstr.setParentBasicBlock(this);
        return icmpInstr;
    }

    public Value createAllocatInstrInFront(LLVMType type) {
        Instruction allocaInstr = new AllocaInstr(type, this);
        allocaInstr.setParentBasicBlock(this);
        if (instructions.size() == 0) {
            instructions.add(allocaInstr);
        } else {
            for (int i = 0; i < instructions.size(); i++) {
                if (i == 0 && !(instructions.get(i) instanceof AllocaInstr)) {
                    instructions.add(0, allocaInstr);
                    break;
                } else if (i == instructions.size() - 1) {
                    instructions.add(allocaInstr);
                    break;
                } else if (instructions.get(i) instanceof AllocaInstr || !(instructions.get(i + 1) instanceof AllocaInstr)) {
                    instructions.add(i + 1, allocaInstr);
                    break;
                }
            }
        }
        return allocaInstr;
    }

    public Value createStoreInstr(Value value, Value ptr) {
        Instruction storeInstr = new StoreInstr(value, ptr, this);
        this.instructions.add(storeInstr);
        storeInstr.setParentBasicBlock(this);
        return storeInstr;
    }

    public Value createBrInstr(BasicBlock trueBranch, BasicBlock falseBranch, Value cond) {
        Instruction brInstr = new BrInstr(cond, trueBranch, falseBranch, this);
        this.instructions.add(brInstr);
        brInstr.setParentBasicBlock(this);
        return brInstr;
    }

    public Value createBrInstr(BasicBlock dest) {
        Instruction brInstr = new BrInstr(dest, this);
        this.instructions.add(brInstr);
        brInstr.setParentBasicBlock(this);
        return brInstr;
    }

    public Value createCallInstr(Function func, ArrayList<Value> args) {
        Instruction callInstr = new CallInstr(func, args, this);
        this.instructions.add(callInstr);
        callInstr.setParentBasicBlock(this);
        return callInstr;
    }

    public Value createGetElementPtrInstr(Value elementBase, ArrayList<Value> offsets) {
        Instruction getelementptrInstr = new GetelementptrInstr(elementBase, offsets, this);
        this.instructions.add(getelementptrInstr);
        getelementptrInstr.setParentBasicBlock(this);
        return getelementptrInstr;
    }

    public Value createLoadInstr(Value ptr) {
        Instruction loadInstr = new LoadInstr(ptr, this);
        this.instructions.add(loadInstr);
        loadInstr.setParentBasicBlock(this);
        return loadInstr;
    }

    public Value createReturnInstr(Value value) {
        Instruction returnInstr;
        if (value == null) {
            returnInstr = new ReturnInstr(this);
        } else {
            returnInstr = new ReturnInstr(value, this);
        }
        this.instructions.add(returnInstr);
        returnInstr.setParentBasicBlock(this);
        return returnInstr;
    }

    public Value createTruncInstr(LLVMType type, Value value) {
        Instruction truncInstr = new TruncInstr(value, type, this);
        this.instructions.add(truncInstr);
        truncInstr.setParentBasicBlock(this);
        return truncInstr;
    }

    public Value createZextInstr(LLVMType type, Value value) {
        Instruction zextInstr = new ZextInstr(value, type, this);
        this.instructions.add(zextInstr);
        zextInstr.setParentBasicBlock(this);
        return zextInstr;
    }

    public Instruction getLastInstruction() {
        return instructions.get(instructions.size() - 1);
    }


    @Override
    public String getName() {
        if (super.nameIsNull()) {
            super.setName("block" + BlockNameProvider.getProvider().alloc());
        }
        return super.getName();
    }

    public void dump(PrintWriter writer) {
        writer.println(this.getName() + ":");
        for (Instruction instr : instructions) {
            instr.dump(writer);
        }

        if (this.instructions.size() == 0 ||
                !(getLastInstruction() instanceof BrInstr) && !(getLastInstruction() instanceof ReturnInstr)) {
            writer.println("  ret void");
        }
        writer.println("");
    }
}
