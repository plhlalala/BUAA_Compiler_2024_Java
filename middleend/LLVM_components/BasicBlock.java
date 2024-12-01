package middleend.LLVM_components;

import middleend.instruction.AllocaInstr;
import middleend.instruction.BinaryInstr;
import middleend.instruction.BinaryOp;
import middleend.instruction.BrInstr;
import middleend.instruction.CallInstr;
import middleend.instruction.GetelementptrInstr;
import middleend.instruction.IcmpInstr;
import middleend.instruction.IcmpOpEnum;
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

public class BasicBlock extends IrValue {
    private Function parentFunction;
    private ArrayList<Instruction> instructions;
    private int loopNum;

    public BasicBlock(Function parentFunction) {
        super(new BasicType(BaseTypeEnum.LABEL, 0));
        this.parentFunction = parentFunction;
        this.instructions = new ArrayList<>();
        loopNum = 0;
    }

    public IrValue createAddInstr(IrValue left, IrValue right) {
        Instruction addInstr = new BinaryInstr(BinaryOp.ADD, left, right, this);
        this.instructions.add(addInstr);
        addInstr.setParentBasicBlock(this);
        return addInstr;
    }

    public IrValue createSubInstr(IrValue left, IrValue right) {
        Instruction subInstr = new BinaryInstr(BinaryOp.SUB, left, right, this);
        this.instructions.add(subInstr);
        subInstr.setParentBasicBlock(this);
        return subInstr;
    }

    public IrValue createMulInstr(IrValue left, IrValue right) {
        Instruction mulInstr = new BinaryInstr(BinaryOp.MUL, left, right, this);
        this.instructions.add(mulInstr);
        mulInstr.setParentBasicBlock(this);
        return mulInstr;
    }

    public IrValue createSDivInstr(IrValue left, IrValue right) {
        Instruction divInstr = new BinaryInstr(BinaryOp.SDIV, left, right, this);
        this.instructions.add(divInstr);
        divInstr.setParentBasicBlock(this);
        return divInstr;
    }

    public IrValue createSRemInstr(IrValue left, IrValue right) {
        Instruction remInstr = new BinaryInstr(BinaryOp.SREM, left, right, this);
        this.instructions.add(remInstr);
        remInstr.setParentBasicBlock(this);
        return remInstr;
    }

    public IrValue createICmpInstr(IcmpOpEnum cond, IrValue left, IrValue right) {
        Instruction icmpInstr = new IcmpInstr(cond, left, right, this);
        this.instructions.add(icmpInstr);
        icmpInstr.setParentBasicBlock(this);
        return icmpInstr;
    }

    public IrValue createAllocatInstrInFront(LLVMType type) {
        Instruction allocaInstr = new AllocaInstr(type, this);
        allocaInstr.setParentBasicBlock(this);
        if (instructions.isEmpty()) {
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

    public IrValue createStoreInstr(IrValue irValue, IrValue ptr) {
        Instruction storeInstr = new StoreInstr(irValue, ptr, this);
        this.instructions.add(storeInstr);
        storeInstr.setParentBasicBlock(this);
        return storeInstr;
    }

    public IrValue createBrInstr(BasicBlock trueBranch, BasicBlock falseBranch, IrValue cond) {
        Instruction brInstr = new BrInstr(cond, trueBranch, falseBranch, this);
        this.instructions.add(brInstr);
        brInstr.setParentBasicBlock(this);
        return brInstr;
    }

    public IrValue createBrInstr(BasicBlock dest) {
        Instruction brInstr = new BrInstr(dest, this);
        this.instructions.add(brInstr);
        brInstr.setParentBasicBlock(this);
        return brInstr;
    }

    public IrValue createCallInstr(Function func, ArrayList<IrValue> args) {
        Instruction callInstr = new CallInstr(func, args, this);
        this.instructions.add(callInstr);
        callInstr.setParentBasicBlock(this);
        return callInstr;
    }

    public IrValue createGetElementPtrInstr(IrValue elementBase, ArrayList<IrValue> offsets) {
        Instruction getelementptrInstr = new GetelementptrInstr(elementBase, offsets, this);
        this.instructions.add(getelementptrInstr);
        getelementptrInstr.setParentBasicBlock(this);
        return getelementptrInstr;
    }

    public IrValue createLoadInstr(IrValue ptr) {
        Instruction loadInstr = new LoadInstr(ptr, this);
        this.instructions.add(loadInstr);
        loadInstr.setParentBasicBlock(this);
        return loadInstr;
    }

    public IrValue createReturnInstr(IrValue irValue) {
        Instruction returnInstr;
        if (irValue == null) {
            returnInstr = new ReturnInstr(this);
        } else {
            returnInstr = new ReturnInstr(irValue, this);
        }
        this.instructions.add(returnInstr);
        returnInstr.setParentBasicBlock(this);
        return returnInstr;
    }

    public IrValue createTruncInstr(LLVMType type, IrValue irValue) {
        Instruction truncInstr = new TruncInstr(irValue, type, this);
        this.instructions.add(truncInstr);
        truncInstr.setParentBasicBlock(this);
        return truncInstr;
    }

    public IrValue createZextInstr(LLVMType type, IrValue irValue) {
        Instruction zextInstr = new ZextInstr(irValue, type, this);
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

        if (this.instructions.isEmpty() ||
                !(getLastInstruction() instanceof BrInstr) && !(getLastInstruction() instanceof ReturnInstr)) {
            this.instructions.add(new ReturnInstr(this));
            writer.println("  ret void");
        }
        writer.println("");
    }

    public ArrayList<AllocaInstr> getAllocaInstrs() {
        ArrayList<AllocaInstr> allocaInstrs = new ArrayList<>();
        for (Instruction instr : instructions) {
            if (instr instanceof AllocaInstr) {
                allocaInstrs.add((AllocaInstr) instr);
            }
        }
        return allocaInstrs;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public String getMIPSLabelName() {
        return this.parentFunction.getName() + "_" + this.getName();
    }
}
