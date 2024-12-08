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
import middleend.instruction.PcopyInstr;
import middleend.instruction.PhiInstr;
import middleend.instruction.ReturnInstr;
import middleend.instruction.StoreInstr;
import middleend.instruction.TruncInstr;
import middleend.instruction.ZextInstr;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock extends IrValue {
    private Function parentFunction;
    private ArrayList<Instruction> instructions;
    private int loopNum;

    private ArrayList<BasicBlock> successors;
    private ArrayList<BasicBlock> predecessors;
    private ArrayList<BasicBlock> domList;
    private BasicBlock paraentDom;
    private ArrayList<BasicBlock> childrenDom;
    private ArrayList<BasicBlock> domFrontierList;

    private HashSet<IrValue> in;
    private HashSet<IrValue> out;
    private HashSet<IrValue> def;
    private HashSet<IrValue> use;

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

    public IrValue createPhiInstrInFront(LLVMType type, ArrayList<BasicBlock> preBlocks) {
        Instruction phiInstr = new PhiInstr(type, preBlocks, this);
        this.instructions.add(0, phiInstr);
        phiInstr.setParentBasicBlock(this);
        return phiInstr;
    }

    public IrValue insertInstrToLast(Instruction instr) {
        this.instructions.add(instr);
        instr.setParentBasicBlock(this);
        return instr;
    }

    public IrValue insertBeforeLast(Instruction instruction) {
        this.instructions.add(this.instructions.size() - 1, instruction);
        instruction.setParentBasicBlock(this);
        return instruction;
    }

    /**
     * 将一个新的中间基本块插入到当前基本块和目标基本块之间，并插入 Pcopy 指令。
     * 同时更新基本块的控制流图，确保前后继关系正确。
     *
     * @param sucblock   目标基本块，当前基本块的后继
     * @param pcopyInstr 要插入的 Pcopy 指令
     */
    public void addBlockAndInsertPcopyInstr(BasicBlock sucblock, PcopyInstr pcopyInstr) {
        BasicBlock mid = new BasicBlock(this.parentFunction);
        this.parentFunction.getBasicBlocks().add(
                this.parentFunction.getBasicBlocks().indexOf(sucblock), mid);
        // 将 Pcopy 指令插入到新基本块的末尾
        mid.insertInstrToLast(pcopyInstr);
        pcopyInstr.setParentBasicBlock(mid);
        // 在新基本块中创建跳转指令，跳转到原目标基本块
        mid.createBrInstr(sucblock);
        BrInstr brInstr = (BrInstr) (this.instructions.get(this.instructions.size() - 1));
        // 判断当前跳转指令的目标（trueBranch 或 falseBranch）是否为目标基本块，若是，则修改为指向中间基本块
        if (brInstr.getTrueBranch().equals(sucblock)) {
            brInstr.setTrueBranch(mid);
        } else {
            brInstr.setFalseBranch(mid);
        }
        this.getSuccessors();
        // 更新目标基本块的前驱节点列表，将当前基本块从前驱中移除，添加新中间基本块
        sucblock.getPredecessors().add(sucblock.getPredecessors().indexOf(this), mid);
        sucblock.getPredecessors().remove(this);
        mid.setSuccessors(new ArrayList<>());
        mid.getSuccessors().add(sucblock);
        mid.setPredecessors(new ArrayList<>());
        mid.getPredecessors().add(this);
    }


    public void buildDefUseChain() {
        def = new HashSet<>();
        use = new HashSet<>();
        for (Instruction instr : instructions) {
            if (instr instanceof PhiInstr) {
                for (IrValue operand : instr.getOperands()) {
                    if (operand instanceof Instruction || operand instanceof FunctionParam || operand instanceof GlobalIrValue) {
                        use.add(operand);
                    }
                }
            } else {
                if (instructions.get(0) instanceof PhiInstr && instructions.get(instructions.indexOf(instr) - 1) instanceof PhiInstr) {
                    for (int i = 0; i < instructions.indexOf(instr); i++) {
                        if (!use.contains(instructions.get(i))) {
                            def.add(instructions.get(i));
                        }
                    }
                }
                for (IrValue operand : instr.getOperands()) {
                    if (!def.contains(operand) && (operand instanceof Instruction || operand instanceof FunctionParam || operand instanceof GlobalIrValue)) {
                        use.add(operand);
                    }
                }
                if (!use.contains(instr) && judgeIsValue(instr)) {
                    def.add(instr);
                }
            }
        }
    }

    public boolean judgeIsValue(Instruction instr) {
        if (instr instanceof AllocaInstr || instr instanceof BinaryInstr || instr instanceof GetelementptrInstr
                || instr instanceof LoadInstr || instr instanceof TruncInstr || instr instanceof ZextInstr
                || instr instanceof PhiInstr || instr instanceof IcmpInstr) {
            return true;
        }
        if (instr instanceof CallInstr callInstr) {
            return callInstr.getFunc().getReturnBaseType() != BaseTypeEnum.VOID;
        }
        return false;
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

    public Function getParentFunction() {
        return parentFunction;
    }

    public String getMIPSLabelName() {
        return this.parentFunction.getName() + "_" + this.getName();
    }

    public ArrayList<BasicBlock> getSuccessors() {
        ArrayList<BasicBlock> successors = new ArrayList<>();
        if (getLastInstruction() instanceof BrInstr) {
            BrInstr brInstr = (BrInstr) getLastInstruction();
            if (brInstr.getCond() == null) {
                successors.add(brInstr.getDest());
            } else {
                successors.add(brInstr.getTrueBranch());
                successors.add(brInstr.getFalseBranch());
            }
        }
        this.successors = successors;
        return successors;
    }

    public void setSuccessors(ArrayList<BasicBlock> successors) {
        this.successors = successors;
    }

    public ArrayList<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(ArrayList<BasicBlock> predecessors) {
        this.predecessors = predecessors;
    }

    public ArrayList<BasicBlock> getDomList() {
        return domList;
    }

    public void setDomList(ArrayList<BasicBlock> domList) {
        this.domList = domList;
    }

    public BasicBlock getParaentDom() {
        return paraentDom;
    }

    public void setParaentDom(BasicBlock paraentDom) {
        this.paraentDom = paraentDom;
    }

    public ArrayList<BasicBlock> getChildrenDom() {
        return childrenDom;
    }

    public void setChildrenDom(ArrayList<BasicBlock> childrenDom) {
        this.childrenDom = childrenDom;
    }

    public ArrayList<BasicBlock> getDomFrontierList() {
        return domFrontierList == null ? new ArrayList<>() : domFrontierList;
    }

    public void setDomFrontierList(ArrayList<BasicBlock> domFrontierList) {
        this.domFrontierList = domFrontierList;
    }

    public HashSet<IrValue> getIn() {
        return in;
    }

    public void setIn(HashSet<IrValue> in) {
        this.in = in;
    }

    public HashSet<IrValue> getOut() {
        return out;
    }

    public void setOut(HashSet<IrValue> out) {
        this.out = out;
    }

    public HashSet<IrValue> getDef() {
        return def;
    }

    public void setDef(HashSet<IrValue> def) {
        this.def = def;
    }

    public HashSet<IrValue> getUse() {
        return use;
    }

    public void setUse(HashSet<IrValue> use) {
        this.use = use;
    }
}
