package backend;

import backend.Data.Data;
import backend.Data.DataType;
import backend.Text.InstrText;
import backend.Text.LabelText;
import backend.Value.MIPSImmediate;
import backend.Value.MIPSLabel;
import backend.Value.MIPSOffset;
import backend.Value.MIPSRegister;
import backend.Value.MIPSValue;
import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.GlobalIrValue;
import middleend.LLVM_components.ImmIrValueBool;
import middleend.LLVM_components.ImmIrValueI32;
import middleend.LLVM_components.ImmIrValueI8;
import middleend.LLVM_components.IrModule;
import middleend.LLVM_components.IrValue;
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
import middleend.instruction.MoveInstr;
import middleend.instruction.ReturnInstr;
import middleend.instruction.StoreInstr;
import middleend.instruction.TruncInstr;
import middleend.instruction.ZextInstr;
import middleend.type.ArrayType;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.util.ArrayList;
import java.util.HashMap;

public class Generator {
    public final MIPSmodule module = new MIPSmodule();
    private final ValueManager valueManager = new ValueManager(module);

    public void generate(IrModule IRmodule) {
        for (GlobalIrValue globalValue : IRmodule.getGlobalVariables()) {
            generateGlobalValue(globalValue);
        }
        module.addText(new InstrText("jal", buildArray(new MIPSLabel(IRmodule.getMainFunction().getName()))));
        module.addText(new InstrText("j", buildArray(new MIPSLabel("exit_exit_exit_exit"))));
        for (Function function : IRmodule.getFunctions()) {
            generateFunction(function);
        }
        generateFunction(IRmodule.getMainFunction());
        module.addText(new LabelText("exit_exit_exit_exit"));
    }

    public void generateGlobalValue(GlobalIrValue globalValue) {
        Data data;
        ArrayList<Integer> initVals = globalValue.getInitVals();
        boolean isArray = globalValue.getTypeOfValue() instanceof ArrayType;
        MIPSLabel label = new MIPSLabel(globalValue.getName().substring(1));
        if (initVals.isEmpty() || isAllZero(initVals)) {
            if (isArray) {
                int mem = ((ArrayType) globalValue.getTypeOfValue()).getArraysize();
                mem = mem * 4;
                data = new Data(label, DataType.SPACE, mem);
            } else {
                data = new Data(label, DataType.WORD, 0);
            }
        } else {
            if (isArray) {
                int size = ((ArrayType) globalValue.getTypeOfValue()).getArraysize();
                ArrayList<Integer> initVal = new ArrayList<>(initVals);
                for (int i = initVals.size(); i < size; i++) {
                    initVal.add(0);
                }
                data = new Data(label, DataType.WORD, initVal);
            } else {
                data = new Data(label, DataType.WORD, initVals.get(0));
            }
        }
        module.addData(data);
        valueManager.addGlobal(globalValue, label);
    }

    public void generateFunction(Function function) {
        module.addText(new LabelText(function.getName()));
        valueManager.reset(function);
        for (int i = 0; i < function.getParams().size(); i++) {
            if (i < 4) {
                valueManager.allocRegForPara(function.getParams().get(i), i);
            }
            valueManager.subOffset(4);
            valueManager.addOffSetValueMap(function.getParams().get(i), valueManager.getOffset());
        }
        if (!function.getParams().isEmpty()) {
            valueManager.replaceReg(function.getParams().get(0), MIPSRegister.GP); // 由于a0可能用于系统调用，所以将第一个参数存储到gp中
        }
        for (BasicBlock block : function.getBasicBlocks()) {
            generateBlock(block);
        }
    }

    public void generateBlock(BasicBlock block) {
        module.addText(new LabelText(" "));
        module.addText(new LabelText(block.getMIPSLabelName()));
        for (Instruction instruction : block.getInstructions()) {
            generateInstruction(instruction);
        }
    }

    public void generateInstruction(Instruction instruction) {
        module.addText(new LabelText(" "));
        module.addText(new LabelText("# " + instruction.dumpToString()));
        if (instruction instanceof AllocaInstr instr) {
            genAlloca(instr);
        } else if (instruction instanceof BinaryInstr instr) {
            genBinary(instr);
        } else if (instruction instanceof BrInstr instr) {
            genBr(instr);
        } else if (instruction instanceof CallInstr instr) {
            genCall(instr);
        } else if (instruction instanceof GetelementptrInstr instr) {
            genGetelementptr(instr);
        } else if (instruction instanceof IcmpInstr instr) {
            genIcmp(instr);
        } else if (instruction instanceof LoadInstr instr) {
            genLoad(instr);
        } else if (instruction instanceof MoveInstr instr) {
            genMove(instr);
        } else if (instruction instanceof ReturnInstr instr) {
            genReturn(instr);
        } else if (instruction instanceof StoreInstr instr) {
            genStore(instr);
        } else if (instruction instanceof TruncInstr instr) {
            genTrunc(instr);
        } else if (instruction instanceof ZextInstr instr) {
            genZext(instr);
        } else {
            System.out.println("Error: Unknown instruction");
        }
    }


    public void genAlloca(AllocaInstr instr) {
        int mem = calculateMem(instr);
        valueManager.subOffset(mem);
        int offset = valueManager.getOffset();
        module.addText(new InstrText("addiu", buildArray(MIPSRegister.K0, MIPSRegister.SP, new MIPSImmediate(offset))));// 计算在栈中的地址

        if (valueManager.getRegOfValue(instr) != null) {
            MIPSRegister reg = valueManager.getRegOfValue(instr);
            module.addText(new InstrText("move", buildArray(reg, MIPSRegister.K0))); // 将地址存储到寄存器中
        } else {
            valueManager.subOffset(4);
            offset = valueManager.getOffset();
            module.addText(new InstrText("sw", buildArray(MIPSRegister.K0, new MIPSOffset(offset)))); // 将地址存储到栈中
            valueManager.addOffSetValueMap(instr, offset);
        }
    }

    public void genBinary(BinaryInstr instr) {
        MIPSValue left = MIPSRegister.K0;
        MIPSValue right = MIPSRegister.K1;
        MIPSRegister result = valueManager.getRegOfValue(instr) == null ? MIPSRegister.K0 : valueManager.getRegOfValue(instr);
        if (instr.getLeft() instanceof ImmIrValueI32 i1 && instr.getRight() instanceof ImmIrValueI32 i2) {
            module.addText(new InstrText("li", buildArray(result, new MIPSImmediate(calculateImm(i1.getValue(), i2.getValue(), instr.getOp())))));
        } else {
            if (instr.getLeft() instanceof ImmIrValueI32 i1) {
                module.addText(new InstrText("li", buildArray(left, new MIPSImmediate(i1.getValue()))));
            } else if (valueManager.getRegOfValue(instr.getLeft()) != null) {
                left = valueManager.getRegOfValue(instr.getLeft());
            } else {
                int offset = valueManager.getOffSetOfValue(instr.getLeft());
                module.addText(new InstrText("lw", buildArray(left, new MIPSOffset(offset))));
            }
            if (instr.getRight() instanceof ImmIrValueI32 i2) {
                right = new MIPSImmediate(i2.getValue());
            } else if (valueManager.getRegOfValue(instr.getRight()) != null) {
                right = valueManager.getRegOfValue(instr.getRight());
            } else {
                int offset = valueManager.getOffSetOfValue(instr.getRight());
                module.addText(new InstrText("lw", buildArray(right, new MIPSOffset(offset))));
            }
            BinaryOp op = instr.getOp();
            switch (op) {
                case ADD -> {
                    InstrText instrText = new InstrText("addu", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case SUB -> {
                    InstrText instrText = new InstrText("subu", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case MUL -> {
                    InstrText instrText = new InstrText("mul", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case SDIV -> {
                    if (right instanceof MIPSImmediate) {
                        module.addText(new InstrText("li", buildArray(MIPSRegister.K1, right)));
                        right = MIPSRegister.K1;
                    }
                    InstrText instrText1 = new InstrText("div", buildArray(left, right));
                    InstrText instrText2 = new InstrText("mflo", buildArray(result));
                    module.addText(instrText1);
                    module.addText(instrText2);
                }
                case SREM -> {
                    if (right instanceof MIPSImmediate) {
                        module.addText(new InstrText("li", buildArray(MIPSRegister.K1, right)));
                        right = MIPSRegister.K1;
                    }
                    InstrText instrText1 = new InstrText("div", buildArray(left, right));
                    InstrText instrText2 = new InstrText("mfhi", buildArray(result));
                    module.addText(instrText1);
                    module.addText(instrText2);
                }
            }
        }
        if (valueManager.getRegOfValue(instr) == null) {
            valueManager.subOffset(4);
            int offset = valueManager.getOffset();
            module.addText(new InstrText("sw", buildArray(result, new MIPSOffset(offset))));
            valueManager.addOffSetValueMap(instr, offset);
        }
    }

    public void genBr(BrInstr instr) {
        if (instr.getCond() == null) {
            InstrText instrText = new InstrText("j", buildArray(new MIPSLabel(instr.getDest().getMIPSLabelName())));
            module.addText(instrText);
        } else if (instr.getCond() instanceof ImmIrValueBool bool) {
            if (bool.getValue() == 1) {
                InstrText instrText = new InstrText("j", buildArray(new MIPSLabel(instr.getTrueBranch().getMIPSLabelName())));
                module.addText(instrText);
            } else {
                InstrText instrText = new InstrText("j", buildArray(new MIPSLabel(instr.getFalseBranch().getMIPSLabelName())));
                module.addText(instrText);
            }
        } else {
            MIPSRegister reg = valueManager.getRegOfValue(instr.getCond());
            if (reg == null) {
                int offset = valueManager.getOffSetOfValue(instr.getCond());
                module.addText(new InstrText("lw", buildArray(MIPSRegister.K0, new MIPSOffset(offset))));
                reg = MIPSRegister.K0;
            }
            InstrText instrText1 = new InstrText("bnez", buildArray(reg, new MIPSLabel(instr.getTrueBranch().getMIPSLabelName())));
            InstrText instrText2 = new InstrText("j", buildArray(new MIPSLabel(instr.getFalseBranch().getMIPSLabelName())));
            module.addText(instrText1);
            module.addText(instrText2);
        }
    }

    public void genCall(CallInstr instr) {
        Function func = instr.getFunc();
        if (func == Function.IRFUNC_GETINT) {
            module.addText(new InstrText("li", buildArray(MIPSRegister.V0, new MIPSImmediate(5))));
            module.addText(new InstrText("syscall", new ArrayList<>()));
            if (valueManager.getRegOfValue(instr) == null) {
                valueManager.subOffset(4);
                int offset = valueManager.getOffset();
                module.addText(new InstrText("sw", buildArray(MIPSRegister.V0, new MIPSOffset(offset))));
                valueManager.addOffSetValueMap(instr, offset);
            } else {
                MIPSRegister reg = valueManager.getRegOfValue(instr);
                module.addText(new InstrText("move", buildArray(reg, MIPSRegister.V0)));
            }
        } else if (func == Function.IRFUNC_GETCHAR) {
            module.addText(new InstrText("li", buildArray(MIPSRegister.V0, new MIPSImmediate(12))));
            module.addText(new InstrText("syscall", new ArrayList<>()));
            if (valueManager.getRegOfValue(instr) == null) {
                valueManager.subOffset(4);
                int offset = valueManager.getOffset();
                module.addText(new InstrText("sw", buildArray(MIPSRegister.V0, new MIPSOffset(offset))));
                valueManager.addOffSetValueMap(instr, offset);
            } else {
                MIPSRegister reg = valueManager.getRegOfValue(instr);
                module.addText(new InstrText("move", buildArray(reg, MIPSRegister.V0)));
            }
        } else if (func == Function.IRFUNC_PUTINT) {
            IrValue value = instr.getArgs().get(0);
            if (value instanceof ImmIrValueI32 i32) {
                module.addText(new InstrText("li", buildArray(MIPSRegister.getReg("a0"), new MIPSImmediate(i32.getValue()))));
            } else if (valueManager.getRegOfValue(value) != null) {
                MIPSRegister reg = valueManager.getRegOfValue(value);
                module.addText(new InstrText("move", buildArray(MIPSRegister.getReg("a0"), reg)));
            } else {
                loadToRegFromStackBasedOnType(MIPSRegister.getReg("a0"), value);
            }
            module.addText(new InstrText("li", buildArray(MIPSRegister.V0, new MIPSImmediate(1))));
            module.addText(new InstrText("syscall", new ArrayList<>()));
        } else if (func == Function.IRFUNC_PUTCHAR) {
            IrValue value = instr.getArgs().get(0);
            if (value instanceof ImmIrValueI32 i32) {
                module.addText(new InstrText("li", buildArray(MIPSRegister.getReg("a0"), new MIPSImmediate(i32.getValue()))));
            } else if (valueManager.getRegOfValue(value) != null) {
                MIPSRegister reg = valueManager.getRegOfValue(value);
                module.addText(new InstrText("move", buildArray(MIPSRegister.getReg("a0"), reg)));
            } else {
                loadToRegFromStackBasedOnType(MIPSRegister.getReg("a0"), value);
            }
            module.addText(new InstrText("li", buildArray(MIPSRegister.V0, new MIPSImmediate(11))));
            module.addText(new InstrText("syscall", new ArrayList<>()));
        } else {
            ArrayList<IrValue> args = instr.getArgs();
            ArrayList<MIPSRegister> regs = valueManager.getAllocatedRegs();
            HashMap<MIPSRegister, Integer> tmpOffsetMap = new HashMap<>();
            int curOffset = valueManager.getOffset();

            for (MIPSRegister reg : regs) {
                valueManager.subOffset(4);
                tmpOffsetMap.put(reg, valueManager.getOffset());
                module.addText(new InstrText("sw", buildArray(reg, new MIPSOffset(valueManager.getOffset()))));
            }
            valueManager.subOffset(4);
            module.addText(new InstrText("sw", buildArray(MIPSRegister.RA, new MIPSOffset(valueManager.getOffset()))));
            valueManager.subOffset(4);
            module.addText(new InstrText("sw", buildArray(MIPSRegister.SP, new MIPSOffset(valueManager.getOffset()))));
            for (int i = 0; i < args.size(); i++) {
                if (i < 4) {
                    if (args.get(i) instanceof ImmIrValueI32 i32) {
                        module.addText(new InstrText("li", buildArray(MIPSRegister.getReg("a" + (i)), new MIPSImmediate(i32.getValue()))));
                    } else { //TODO 可能有点问题
                        MIPSRegister reg = valueManager.getRegOfValue(args.get(i));
                        if (reg == null) {
                            loadToRegFromStackBasedOnType(MIPSRegister.getReg("a" + (i)), args.get(i));
                        } else if (reg.toString().contains("a") || reg.toString().contains("gp")) {
                            module.addText(new InstrText("lw", buildArray(MIPSRegister.getReg("a" + (i)), new MIPSOffset(tmpOffsetMap.get(reg)))));
                        } else {
                            module.addText(new InstrText("move", buildArray(MIPSRegister.getReg("a" + (i)), reg)));
                        }
                    }
                    valueManager.subOffset(4);
                    if (i == 0) {
                        module.addText(new InstrText("move", buildArray(MIPSRegister.GP, MIPSRegister.getReg("a0"))));
                    }
                } else {
                    if (args.get(i) instanceof ImmIrValueI32 i32) {
                        module.addText(new InstrText("li", buildArray(MIPSRegister.K0, new MIPSImmediate(i32.getValue()))));
                        valueManager.subOffset(4);
                        module.addText(new InstrText("sw", buildArray(MIPSRegister.K0, new MIPSOffset(valueManager.getOffset()))));
                    } else {
                        MIPSRegister reg = valueManager.getRegOfValue(args.get(i));
                        if (reg == null) {
                            loadToRegFromStackBasedOnType(MIPSRegister.K0, args.get(i));
                            valueManager.subOffset(4);
                            module.addText(new InstrText("sw", buildArray(MIPSRegister.K0, new MIPSOffset(valueManager.getOffset()))));
                        } else {
                            valueManager.subOffset(4);
                            module.addText(new InstrText("sw", buildArray(reg, new MIPSOffset(valueManager.getOffset()))));
                        }
                    }
                }
            }
            module.addText(new InstrText("addiu", buildArray(MIPSRegister.SP, MIPSRegister.SP, new MIPSImmediate(curOffset - 4 * regs.size() - 8))));
            module.addText(new InstrText("jal", buildArray(new MIPSLabel(func.getName()))));
            module.addText(new InstrText("lw", buildArray(MIPSRegister.SP, new MIPSOffset(0))));
            for (int i = 0; i < regs.size(); i++) {
                module.addText(new InstrText("lw", buildArray(regs.get(i), new MIPSOffset(curOffset - 4 * (i + 1)))));
            }
            module.addText(new InstrText("lw", buildArray(MIPSRegister.RA, new MIPSOffset(curOffset - 4 * (regs.size() + 1)))));
            valueManager.setOffset(curOffset);

            if (func.getReturnBaseType() != BaseTypeEnum.VOID) {
                if (valueManager.getRegOfValue(instr) != null) {
                    MIPSRegister reg = valueManager.getRegOfValue(instr);
                    module.addText(new InstrText("move", buildArray(reg, MIPSRegister.V0)));
                } else {
                    valueManager.subOffset(4);
                    int offset = valueManager.getOffset();
                    module.addText(new InstrText("sw", buildArray(MIPSRegister.V0, new MIPSOffset(offset))));
                    valueManager.addOffSetValueMap(instr, offset);
                }
            }
        }
    }

    public void genGetelementptr(GetelementptrInstr instr) {
        IrValue basePtr = instr.getBasePtr();
        ArrayList<IrValue> offsets = instr.getOffsets();
        IrValue offset = offsets.size() == 2 ? offsets.get(1) : offsets.get(0);// 如果为2，第一个偏移必为0
        MIPSRegister baseReg = MIPSRegister.K0;
        MIPSRegister offsetReg = MIPSRegister.K1;
        MIPSRegister resultReg = valueManager.getRegOfValue(instr) == null ? MIPSRegister.K0 : valueManager.getRegOfValue(instr);

        if (basePtr instanceof GlobalIrValue globalIrValue) {
            module.addText(new InstrText("la", buildArray(baseReg, valueManager.getLabelOfGlobal(globalIrValue))));
        } else if (valueManager.getRegOfValue(basePtr) != null) { // TODO 是不是可以换一下顺序，全局变量的地址也有可能被分配到寄存器里
            baseReg = valueManager.getRegOfValue(basePtr);
        } else {
            int offsetInStack = valueManager.getOffSetOfValue(basePtr);
            module.addText(new InstrText("lw", buildArray(baseReg, new MIPSOffset(offsetInStack))));
        }

        if (offset instanceof ImmIrValueI32 i32) {
            module.addText(new InstrText("addu", buildArray(resultReg, baseReg, new MIPSImmediate(i32.getValue() * 4))));
        } else if (valueManager.getRegOfValue(offset) != null) {
            offsetReg = valueManager.getRegOfValue(offset);
            module.addText(new InstrText("sll", buildArray(MIPSRegister.K1, offsetReg, new MIPSImmediate(2))));
            module.addText(new InstrText("addu", buildArray(resultReg, baseReg, MIPSRegister.K1)));
        } else {
            int offsetInStack = valueManager.getOffSetOfValue(offset);
            module.addText(new InstrText("lw", buildArray(offsetReg, new MIPSOffset(offsetInStack))));
            module.addText(new InstrText("sll", buildArray(offsetReg, offsetReg, new MIPSImmediate(2))));
            module.addText(new InstrText("addu", buildArray(resultReg, baseReg, offsetReg)));
        }

        if (valueManager.getRegOfValue(instr) == null) {
            valueManager.subOffset(4);
            int curoffset = valueManager.getOffset();
            module.addText(new InstrText("sw", buildArray(resultReg, new MIPSOffset(curoffset))));
            valueManager.addOffSetValueMap(instr, curoffset);
        }
    }

    public void genIcmp(IcmpInstr instr) {
        MIPSValue left = MIPSRegister.K0;
        MIPSValue right = MIPSRegister.K1;
        MIPSRegister result = valueManager.getRegOfValue(instr) == null ? MIPSRegister.K0 : valueManager.getRegOfValue(instr);
        if (instr.getLeft() instanceof ImmIrValueI32 i1 && instr.getRight() instanceof ImmIrValueI32 i2) {
            module.addText(new InstrText("li", buildArray(result, new MIPSImmediate(calculateImm(i1.getValue(), i2.getValue(), instr.getCond())))));
        } else {
            if (instr.getLeft() instanceof ImmIrValueI32 i1) {
                module.addText(new InstrText("li", buildArray(left, new MIPSImmediate(i1.getValue()))));
            } else if (valueManager.getRegOfValue(instr.getLeft()) != null) {
                left = valueManager.getRegOfValue(instr.getLeft());
            } else {
                int offset = valueManager.getOffSetOfValue(instr.getLeft());
                module.addText(new InstrText("lw", buildArray(left, new MIPSOffset(offset))));
            }

            if (instr.getRight() instanceof ImmIrValueI32 i2) {
                if (instr.getCond() == IcmpOpEnum.SLT) {
                    module.addText(new InstrText("li", buildArray(right, new MIPSImmediate(i2.getValue()))));
                } else right = new MIPSImmediate(i2.getValue());
            } else if (valueManager.getRegOfValue(instr.getRight()) != null) {
                right = valueManager.getRegOfValue(instr.getRight());
            } else {
                int offset = valueManager.getOffSetOfValue(instr.getRight());
                module.addText(new InstrText("lw", buildArray(right, new MIPSOffset(offset))));
            }
            IcmpOpEnum op = instr.getCond();
            switch (op) {
                case EQ -> {
                    InstrText instrText = new InstrText("seq", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case NE -> {
                    InstrText instrText = new InstrText("sne", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case SGT -> {
                    InstrText instrText = new InstrText("sgt", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case SLT -> {
                    InstrText instrText = new InstrText("slt", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case SGE -> {
                    InstrText instrText = new InstrText("sge", buildArray(result, left, right));
                    module.addText(instrText);
                }
                case SLE -> {
                    InstrText instrText = new InstrText("sle", buildArray(result, left, right));
                    module.addText(instrText);
                }
            }
        }
        if (valueManager.getRegOfValue(instr) == null) {
            valueManager.subOffset(4);
            int offset = valueManager.getOffset();
            module.addText(new InstrText("sw", buildArray(result, new MIPSOffset(offset))));
            valueManager.addOffSetValueMap(instr, offset);
        }
    }

    public void genLoad(LoadInstr instr) {
        IrValue pointer = instr.getPointer();
        MIPSRegister pointerReg = MIPSRegister.K1;
        MIPSRegister reg = valueManager.getRegOfValue(instr) == null ? MIPSRegister.K0 : valueManager.getRegOfValue(instr);
        if (pointer instanceof GlobalIrValue globalIrValue) {
            module.addText(new InstrText("la", buildArray(pointerReg, valueManager.getLabelOfGlobal(globalIrValue))));
        } else if (valueManager.getRegOfValue(pointer) != null) {
            pointerReg = valueManager.getRegOfValue(pointer);
        } else {
            int offset = valueManager.getOffSetOfValue(pointer);
            module.addText(new InstrText("lw", buildArray(pointerReg, new MIPSOffset(offset))));
        }

        if (pointer.getTypeOfValue().getPtrNum() == 2) {
            module.addText(new InstrText("lw", buildArray(reg, new MIPSOffset(pointerReg, 0))));
        } else {
            if (pointer.getTypeOfValue().getBaseType() == BaseTypeEnum.CHAR) {
                module.addText(new InstrText("lb", buildArray(reg, new MIPSOffset(pointerReg, 0))));
            } else if (pointer.getTypeOfValue().getBaseType() == BaseTypeEnum.INT) {
                module.addText(new InstrText("lw", buildArray(reg, new MIPSOffset(pointerReg, 0))));
            } else if (pointer.getTypeOfValue().getBaseType() == BaseTypeEnum.BOOL) {
                module.addText(new InstrText("lw", buildArray(reg, new MIPSOffset(pointerReg, 0))));
            }
        }
        // 应该根据指针指向的类型进行判断
        if (valueManager.getRegOfValue(instr) == null) {
            valueManager.subOffset(4);
            int offset = valueManager.getOffset();
            module.addText(new InstrText("sw", buildArray(reg, new MIPSOffset(offset))));
            valueManager.addOffSetValueMap(instr, offset);
        }
    }


    private void genMove(MoveInstr instr) {
        MIPSRegister srcReg = valueManager.getRegOfValue(instr.getSrc()) == null ? MIPSRegister.K0 : valueManager.getRegOfValue(instr.getSrc());
        MIPSRegister destReg = valueManager.getRegOfValue(instr.getDst()) == null ? MIPSRegister.K1 : valueManager.getRegOfValue(instr.getDst());
        if (srcReg == destReg) {
            module.addText(new InstrText("nop", new ArrayList<>()));
            return;
        }
        if (instr.getSrc() instanceof ImmIrValueI32 || instr.getSrc() instanceof ImmIrValueI8 || instr.getSrc() instanceof ImmIrValueBool) {
            module.addText(new InstrText("li", buildArray(destReg, new MIPSImmediate(Integer.parseInt(instr.getSrc().getName())))));
        } else if (srcReg != MIPSRegister.K0) {
            module.addText(new InstrText("move", buildArray(destReg, srcReg)));
        } else {
            if (valueManager.containsValueOffset(instr.getSrc())) {
                int offset = valueManager.getOffSetOfValue(instr.getSrc());
                module.addText(new InstrText("lw", buildArray(destReg, new MIPSOffset(offset))));
            } else {
                // 可能在后面定义，先分配位置，就后面再存储
                valueManager.subOffset(4);
                int offset = valueManager.getOffset();
                valueManager.addOffSetValueMap(instr.getSrc(), offset);
                module.addText(new InstrText("lw", buildArray(destReg, new MIPSOffset(offset))));
            }
        }
        if (destReg == MIPSRegister.K1) {
            if (valueManager.containsValueOffset(instr.getDst())) {
                int offset = valueManager.getOffSetOfValue(instr.getDst());
                module.addText(new InstrText("sw", buildArray(destReg, new MIPSOffset(offset))));
            } else {
                valueManager.subOffset(4);
                int offset = valueManager.getOffset();
                valueManager.addOffSetValueMap(instr.getDst(), offset);
                module.addText(new InstrText("sw", buildArray(destReg, new MIPSOffset(offset))));
            }
        }
    }

    public void genReturn(ReturnInstr instr) {
        IrValue retValue = instr.getReturn();
        if (retValue instanceof ImmIrValueI32 i32) { //TODO i8立即数没处理完
            module.addText(new InstrText("li", buildArray(MIPSRegister.V0, new MIPSImmediate(i32.getValue()))));
        } else if (retValue instanceof ImmIrValueI8 i8) {
            module.addText(new InstrText("li", buildArray(MIPSRegister.V0, new MIPSImmediate(i8.getValue()))));
        } else {
            if (retValue != null) {
                MIPSRegister reg = valueManager.getRegOfValue(retValue);
                if (reg == null) {
                    loadToRegFromStackBasedOnType(MIPSRegister.V0, retValue);
                } else {
                    module.addText(new InstrText("move", buildArray(MIPSRegister.V0, reg)));
                }
            }
        }
        module.addText(new InstrText("jr", buildArray(MIPSRegister.RA)));
    }

    public void genStore(StoreInstr instr) {
        IrValue value = instr.getValue();
        IrValue pointer = instr.getPointer();
        MIPSRegister valueReg = MIPSRegister.K0;
        MIPSRegister pointerReg = MIPSRegister.K1;
        if (value instanceof ImmIrValueI32 i32) {
            module.addText(new InstrText("li", buildArray(valueReg, new MIPSImmediate(i32.getValue()))));
        } else if (value instanceof ImmIrValueI8 i8) {
            module.addText(new InstrText("li", buildArray(valueReg, new MIPSImmediate(i8.getValue()))));
        } else if (valueManager.getRegOfValue(value) != null) {
            valueReg = valueManager.getRegOfValue(value);
        } else {
            loadToRegFromStackBasedOnType(valueReg, value);
        }

        if (pointer instanceof GlobalIrValue globalIrValue) {
            module.addText(new InstrText("la", buildArray(pointerReg, valueManager.getLabelOfGlobal(globalIrValue))));

        } else if (valueManager.getRegOfValue(pointer) != null) {
            pointerReg = valueManager.getRegOfValue(pointer);
        } else {
            int offset = valueManager.getOffSetOfValue(pointer);
            module.addText(new InstrText("lw", buildArray(pointerReg, new MIPSOffset(offset))));
        }
        module.addText(new InstrText("sw", buildArray(valueReg, new MIPSOffset(pointerReg, 0))));
    }

    public void genTrunc(TruncInstr instr) {
        IrValue value = instr.getIrValue();
        if (value.getTypeOfValue().getBaseType() == BaseTypeEnum.INT && instr.getDestType().getBaseType() == BaseTypeEnum.CHAR) {
            MIPSRegister srcReg = valueManager.getRegOfValue(value);
            MIPSRegister destReg = valueManager.getRegOfValue(instr);
            if (srcReg != null) {
                if (destReg != null) {
                    module.addText(new InstrText("andi", buildArray(destReg, srcReg, new MIPSImmediate(0xff))));
                } else {
                    valueManager.subOffset(4);
                    int offset = valueManager.getOffset();
                    module.addText(new InstrText("sw", buildArray(srcReg, new MIPSOffset(offset))));
                    valueManager.addOffSetValueMap(instr, offset);
                }
            } else {
                if (destReg != null) {
                    if (value instanceof ImmIrValueI32 i32) {
                        module.addText(new InstrText("li", buildArray(destReg, new MIPSImmediate(i32.getValue() & 0xff))));
                    } else {
                        int offset = valueManager.getOffSetOfValue(value);
                        module.addText(new InstrText("lb", buildArray(destReg, new MIPSOffset(offset))));
                    }
                } else {
                    if (value instanceof ImmIrValueI32 i32) {
                        int truncValue = i32.getValue() & 0xff;
                        module.addText(new InstrText("li", buildArray(MIPSRegister.K0, new MIPSImmediate(truncValue))));
                        valueManager.subOffset(4);
                        int offset = valueManager.getOffset();
                        module.addText(new InstrText("sb", buildArray(MIPSRegister.K0, new MIPSOffset(offset))));
                    } else {
                        int offset = valueManager.getOffSetOfValue(value);
                        valueManager.addOffSetValueMap(instr, offset);
                    }
                }
            }
        }
    }

    public void genZext(ZextInstr instr) {
        IrValue value = instr.getIrValue();
        LLVMType dstType = instr.getDestType();
        if (value.getTypeOfValue().getBaseType() != BaseTypeEnum.INT && dstType.getBaseType() == BaseTypeEnum.INT) {
            MIPSRegister srcReg = valueManager.getRegOfValue(value);
            MIPSRegister dstReg = valueManager.getRegOfValue(instr);
            if (dstReg != null) {
                if (srcReg != null) {
                    module.addText(new InstrText("move", buildArray(dstReg, srcReg)));
                } else {
                    if (value instanceof ImmIrValueBool bool) {
                        int zextValue = bool.getValue() == 0 ? 0 : 1;
                        module.addText(new InstrText("li", buildArray(dstReg, new MIPSImmediate(zextValue))));
                    } else if (value instanceof ImmIrValueI8 i8) {
                        int zextValue = i8.getValue() & 0xff;
                        module.addText(new InstrText("li", buildArray(dstReg, new MIPSImmediate(zextValue))));
                    } else {
                        int offset = valueManager.getOffSetOfValue(value);
                        module.addText(new InstrText("lw", buildArray(dstReg, new MIPSOffset(offset))));
                    }
                }
            } else {
                if (srcReg != null) {
                    valueManager.subOffset(4);
                    int offset = valueManager.getOffset();
                    module.addText(new InstrText("sw", buildArray(srcReg, new MIPSOffset(offset))));
                    valueManager.addOffSetValueMap(instr, offset);
                } else {
                    if (value instanceof ImmIrValueBool bool) {
                        int zextValue = bool.getValue() == 0 ? 0 : 1;
                        module.addText(new InstrText("li", buildArray(MIPSRegister.K0, new MIPSImmediate(zextValue))));
                        valueManager.subOffset(4);
                        int offset = valueManager.getOffset();
                        module.addText(new InstrText("sw", buildArray(MIPSRegister.K0, new MIPSOffset(offset))));
                        valueManager.addOffSetValueMap(instr, offset);
                    } else if (value instanceof ImmIrValueI8 i8) {
                        int zextValue = i8.getValue() & 0xff;
                        module.addText(new InstrText("li", buildArray(MIPSRegister.K0, new MIPSImmediate(zextValue))));
                        valueManager.subOffset(4);
                        int offset = valueManager.getOffset();
                        module.addText(new InstrText("sw", buildArray(MIPSRegister.K0, new MIPSOffset(offset))));
                        valueManager.addOffSetValueMap(instr, offset);
                    } else {
                        int offset = valueManager.getOffSetOfValue(value);
                        valueManager.addOffSetValueMap(instr, offset);
                    }
                }
            }
        }
    }

    public void loadToRegFromStackBasedOnType(MIPSRegister reg, IrValue irValue) {
        if (irValue instanceof ImmIrValueI32 i32) {
            module.addText(new InstrText("li", buildArray(reg, new MIPSImmediate(i32.getValue()))));
        } else if (irValue instanceof ImmIrValueI8 i8) {
            module.addText(new InstrText("li", buildArray(reg, new MIPSImmediate(i8.getValue()))));
        } else {
            if (irValue.getTypeOfValue().getPtrNum() >= 1) {
                int offset = valueManager.getOffSetOfValue(irValue);
                module.addText(new InstrText("lw", buildArray(reg, new MIPSOffset(offset))));
            } else {
                int offset = valueManager.getOffSetOfValue(irValue);
                if (irValue.getTypeOfValue().getBaseType() == BaseTypeEnum.CHAR) {
                    module.addText(new InstrText("lb", buildArray(reg, new MIPSOffset(offset))));
                } else if (irValue.getTypeOfValue().getBaseType() == BaseTypeEnum.INT) {
                    module.addText(new InstrText("lw", buildArray(reg, new MIPSOffset(offset))));
                } else {
                    System.out.println("Error: loadToRegFromStackBasedOnType");
                }
            }
        }

    }

    private boolean isAllZero(ArrayList<Integer> initVals) {
        for (int i : initVals) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    public int calculateMem(IrValue irValue) {
        if (irValue.getTypeOfValue() instanceof ArrayType arrayType) {
            int len = arrayType.getArraysize();
            return len * 4;
        } else if (irValue.getTypeOfValue() instanceof BasicType) {
            return 4;
        } else {
            System.out.println("Error: calculateMem");
            return -1;
        }
    }

    public int calculateImm(int op1, int op2, BinaryOp op) {
        switch (op) {
            case ADD -> {
                return op1 + op2;
            }
            case SUB -> {
                return op1 - op2;
            }
            case MUL -> {
                return op1 * op2;
            }
            case SDIV -> {
                return op1 / op2;
            }
            case SREM -> {
                return op1 % op2;
            }
            default -> {
                System.out.println("Error: calculateImm");
                return -1;
            }
        }
    }

    public int calculateImm(int op1, int op2, IcmpOpEnum op) {
        switch (op) {
            case EQ -> {
                return op1 == op2 ? 1 : 0;
            }
            case NE -> {
                return op1 != op2 ? 1 : 0;
            }
            case SGT -> {
                return op1 > op2 ? 1 : 0;
            }
            case SLT -> {
                return op1 < op2 ? 1 : 0;
            }
            case SGE -> {
                return op1 >= op2 ? 1 : 0;
            }
            case SLE -> {
                return op1 <= op2 ? 1 : 0;
            }
            default -> {
                System.out.println("Error: calculateImm");
                return -1;
            }
        }
    }


    public static ArrayList<MIPSValue> buildArray(MIPSValue value1, MIPSValue value2) {
        ArrayList<MIPSValue> values = buildArray(value1);
        values.add(value2);
        return values;
    }

    public static ArrayList<MIPSValue> buildArray(MIPSValue value1, MIPSValue value2, MIPSValue value3) {
        ArrayList<MIPSValue> values = buildArray(value1, value2);
        values.add(value3);
        return values;
    }

    public static ArrayList<MIPSValue> buildArray(MIPSValue value) {
        ArrayList<MIPSValue> values = new ArrayList<>();
        values.add(value);
        return values;
    }
}
