package backend.Value;

public class MIPSOffset extends MIPSValue {
    private final MIPSRegister reg;
    private int offset;

    public MIPSOffset(MIPSRegister reg, int offset) {
        this.reg = reg;
        this.offset = offset;
    }

    public MIPSOffset(int offset) {
        this.reg = MIPSRegister.SP;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return offset + "(" + reg + ")";
    }
}
