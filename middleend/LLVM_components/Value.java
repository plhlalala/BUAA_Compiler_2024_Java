package middleend.LLVM_components;

import middleend.type.LLVMType;

import java.util.ArrayList;

public abstract class Value {
    private LLVMType type;
    private String name;
    private ArrayList<Use> useList = new ArrayList<>();

    public Value(LLVMType type) {
        this.type = type;
    }

    public Value(LLVMType type, String name) {
        this.type = type;
        this.name = name;
    }

    public LLVMType getTypeOfValue() {
        return type;
    }

    public String getName() {
        if (name == null) {
            name = NameProvider.getProvider().alloc();
        }
        return name;
    }

    public boolean nameIsNull() {
        return name == null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addUse(User user, int pos) {
        useList.add(new Use(user, this, pos));
    }

    public ArrayList<Use> getUseList() {
        return useList;
    }

    @Override
    public String toString() {
        return getTypeOfValue().toString() + " " + getName();
    }
}
