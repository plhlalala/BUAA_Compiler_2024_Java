package backend.Value;

import java.util.HashMap;
import java.util.LinkedList;

public class MIPSRegister extends MIPSValue {
    public static final MIPSRegister ZERO;
    public static final MIPSRegister SP;
    public static final MIPSRegister RA;
    public static final MIPSRegister V0;
    public static final MIPSRegister V1;
    public static final MIPSRegister K0;
    public static final MIPSRegister K1;
    public static final MIPSRegister GP;
    public static final MIPSRegister FP;
    public static final HashMap<String, MIPSRegister> registerMap = new HashMap<>();

    static {
        ZERO = new MIPSRegister("zero");
        SP = new MIPSRegister("sp");
        RA = new MIPSRegister("ra");
        V0 = new MIPSRegister("v0");
        V1 = new MIPSRegister("v1");
        K0 = new MIPSRegister("k0");
        K1 = new MIPSRegister("k1");
        GP = new MIPSRegister("gp");
        FP = new MIPSRegister("fp");
        for (int i = 0; i < 4; i++) {
            new MIPSRegister("a" + i);
        }
        for (int i = 0; i < 9; i++) {
            new MIPSRegister("t" + i);
        }
        for (int i = 0; i < 8; i++) {
            new MIPSRegister("s" + i);
        }
        for (int i = 0; i < 2; i++) {
            new MIPSRegister("k" + i);
        }
    }

    private final String registerName;

    private MIPSRegister(String registerName) {
        this.registerName = registerName;
        if (!registerMap.containsKey(registerName)) {
            registerMap.put(registerName, this);
        }
    }

    public static LinkedList<MIPSRegister> getSRegs() {
        LinkedList<MIPSRegister> sRegs = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            sRegs.add(registerMap.get("s" + i));
        }
        return sRegs;
    }

    public static LinkedList<MIPSRegister> getTRegs() {
        LinkedList<MIPSRegister> tRegs = new LinkedList<>();
        for (int i = 0; i < 9; i++) {
            tRegs.add(registerMap.get("t" + i));
        }
        return tRegs;
    }

    public static MIPSRegister getReg(String regName) {
        return registerMap.getOrDefault(regName, null);
    }

    public static LinkedList<MIPSRegister> getTregAndSreg() {
        LinkedList<MIPSRegister> tRegs = getTRegs();
        LinkedList<MIPSRegister> sRegs = getSRegs();
        tRegs.addAll(sRegs);
        return tRegs;
    }

    public static LinkedList<MIPSRegister> getKregs() {
        LinkedList<MIPSRegister> kRegs = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            kRegs.add(registerMap.get("k" + i));
        }
        return kRegs;
    }

    @Override
    public String toString() {
        return "$" + registerName;
    }
}
