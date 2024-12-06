package middleend.LLVM_components;

import backend.Value.MIPSRegister;
import middleend.instruction.AllocaInstr;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Function extends IrValue {
    //declare i32 @getint()
    //declare i32 @getchar()
    //declare void @putint(i32)
    //declare void @putch(i8)
    //declare void @putstr(i8*)
    public static Function IRFUNC_GETINT = new Function(new BasicType(BaseTypeEnum.INT, 0), new ArrayList<>(), "getint");
    public static Function IRFUNC_GETCHAR = new Function(new BasicType(BaseTypeEnum.INT, 0), new ArrayList<>(), "getchar");
    public static Function IRFUNC_PUTINT = new Function(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList(List.of(new BasicType(BaseTypeEnum.INT, 0))), "putint");
    public static Function IRFUNC_PUTCHAR = new Function(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList(List.of(new BasicType(BaseTypeEnum.INT, 0))), "putch");
    public static Function IRFUNC_PUTSTR = new Function(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList(List.of(new BasicType(BaseTypeEnum.CHAR, 1))), "putstr");

    private LLVMType returnType;
    private ArrayList<FunctionParam> params;
    private ArrayList<BasicBlock> basicBlocks;

    private HashMap<BasicBlock, ArrayList<BasicBlock>> preMap;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> sucMap;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> domMap;
    private HashMap<BasicBlock, BasicBlock> parentMap;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> childMap;

    private HashMap<IrValue, MIPSRegister> value2Reg;

    public Function(LLVMType returnType, ArrayList<FunctionParam> params, String name) {
        super(returnType);
        this.returnType = returnType;
        this.params = params;
        this.setName(name);
        this.basicBlocks = new ArrayList<>();
    }

    public LLVMType getReturnType() {
        return returnType;
    }

    public BaseTypeEnum getReturnBaseType() {
        if (returnType instanceof BasicType basicType) {
            return basicType.getBaseType();
        } else {
            return null;
        }
    }

    public ArrayList<FunctionParam> getParams() {
        return params;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ArrayList<AllocaInstr> getAllocaInstrs() {
        ArrayList<AllocaInstr> allocaInstrs = new ArrayList<>();
        for (BasicBlock basicBlock : basicBlocks) {
            allocaInstrs.addAll(basicBlock.getAllocaInstrs());
        }
        return allocaInstrs;
    }

    public BasicBlock createBasicBlock() {
        BasicBlock basicBlock = new BasicBlock(this);
        basicBlocks.add(basicBlock);
        return basicBlock;
    }

    @Override
    public String toString() {
        return "@" + getName();
    }

    public void dump(PrintWriter writer) {
        writer.printf("define dso_local %s %s(", returnType.toString(), this.toString());
        for (int i = 0; i < params.size(); i++) {
            params.get(i).dump(writer);
            if (i != params.size() - 1) {
                writer.print(", ");
            }
        }
        writer.println(") {");
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.dump(writer);
        }
        writer.println("}");
    }

    public void setPreMap(HashMap<BasicBlock, ArrayList<BasicBlock>> preMap) {
        this.preMap = preMap;
    }

    public void setSucMap(HashMap<BasicBlock, ArrayList<BasicBlock>> sucMap) {
        this.sucMap = sucMap;
    }

    public void setDomMap(HashMap<BasicBlock, ArrayList<BasicBlock>> domMap) {
        this.domMap = domMap;
    }

    public void setParentMap(HashMap<BasicBlock, BasicBlock> parentMap) {
        this.parentMap = parentMap;
    }

    public void setChildMap(HashMap<BasicBlock, ArrayList<BasicBlock>> childMap) {
        this.childMap = childMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getPreMap() {
        return preMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getSucMap() {
        return sucMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getDomMap() {
        return domMap;
    }

    public HashMap<BasicBlock, BasicBlock> getParentMap() {
        return parentMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getChildMap() {
        return childMap;
    }

    public void setValue2Reg(HashMap<IrValue, MIPSRegister> value2Reg) {
        this.value2Reg = value2Reg;
    }

    public HashMap<IrValue, MIPSRegister> getValue2Reg() {
        return value2Reg;
    }
}