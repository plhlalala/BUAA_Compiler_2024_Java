package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;
import middleend.type.ArrayType;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class GetelementptrInstr extends Instruction {
    private Value elementBase;
    private ArrayList<Value> offsets;

    public GetelementptrInstr(Value elementBase, ArrayList<Value> offsets, BasicBlock parentbasicBlock) {
        super(getGepType(elementBase, offsets), new ArrayList<>(), parentbasicBlock); // 类型是该地址对应的类型
        this.elementBase = elementBase;
        this.offsets = offsets;
        super.addOperands(offsets);
        super.addOperand(elementBase);
    }

    public static LLVMType getGepType(Value elementBase, ArrayList<Value> offsets) {
        if (elementBase.getTypeOfValue() instanceof BasicType basicType) {
            return basicType.getTypeClone();
        } else if (elementBase.getTypeOfValue() instanceof ArrayType arrayType) {
            if (offsets.size() == 1) {
                return new ArrayType(arrayType.getBasicTypeClone(), arrayType.getArraysize(), 1);
            } else if (offsets.size() == 2) {
                return new BasicType(arrayType.getBasicTypeClone().getBaseType(), 1);
            } else {
                System.out.println("数组维数有问题？");
                return null;
            }
        } else {
            System.out.println("类型有问题？");
            return null;
        }
    }

    // %ptr = getelementptr i32, i32* %val, i32 0
    // %elem_ptr = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 3
    // %elem_ptr = getelementptr [3 x [4 x i32]], [3 x [4 x i32]]* %array, i32 0, i32 2, i32 3

    public void dump(PrintWriter writer) {
        writer.printf("  %s = getelementptr %s, %s %s",
                this.getName(),
                elementBase.getTypeOfValue().getTypeClone().subPtr().toString(),
                elementBase.getTypeOfValue().toString(),
                elementBase.getName());
        for (Value offset : offsets) {
            writer.printf(", %s %s", offset.getTypeOfValue().toString(), offset.getName());
        }
        writer.print("\n");
    }
}
