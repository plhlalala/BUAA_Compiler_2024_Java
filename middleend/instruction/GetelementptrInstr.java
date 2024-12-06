package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.ArrayType;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GetelementptrInstr extends Instruction {
    private IrValue basePtr;
    private List<IrValue> offsets;

    public GetelementptrInstr(IrValue basePtr, ArrayList<IrValue> offsets, BasicBlock parentbasicBlock) {
        super(getGepType(basePtr, offsets), new ArrayList<>(), parentbasicBlock); // 类型是该地址对应的类型
        super.addOperand(basePtr);
        super.addOperands(offsets);
        this.offsets = super.getOperands().subList(1, super.getOperands().size());
    }

    public static LLVMType getGepType(IrValue elementBase, ArrayList<IrValue> offsets) {
        if (elementBase.getTypeOfValue() instanceof BasicType basicType) {
            return basicType.getTypeClone();
            // 从数组中的某个元素的地址开始（通常作为参数传入的是o号元素），再往后偏移，类型为类似i32*
        } else if (elementBase.getTypeOfValue() instanceof ArrayType arrayType) {
            if (offsets.size() == 1) { //TODO 好像存在问题，貌似不会这样传入参数
                return new ArrayType(arrayType.getBasicTypeClone(), arrayType.getArraysize(), 1);
            } else if (offsets.size() == 2) {
                return new BasicType(arrayType.getBasicTypeClone().getBaseType(), 1);
                // 跟两个参数，表示从数组中获得某个元素的地址，类型为类似i32*,为basicType
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
        getBasePtr();
        writer.printf("  %s = getelementptr %s, %s %s",
                this.getName(),
                basePtr.getTypeOfValue().getTypeClone().subPtr().toString(),
                basePtr.getTypeOfValue().toString(),
                basePtr.getName());
        for (IrValue offset : offsets) {
            writer.printf(", %s %s", offset.getTypeOfValue().toString(), offset.getName());
        }
        writer.print("\n");
    }

    public String dumpToString() {
        getBasePtr();
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName()).append(" = getelementptr ").append(basePtr.getTypeOfValue().getTypeClone().subPtr().toString()).append(", ")
                .append(basePtr.getTypeOfValue().toString()).append(" ").append(basePtr.getName());
        for (IrValue offset : offsets) {
            sb.append(", ").append(offset.getTypeOfValue().toString()).append(" ").append(offset.getName());
        }
        return sb.toString();
    }

    public IrValue getBasePtr() {
        this.basePtr = super.getOperands().get(0);
        return basePtr;
    }

    public ArrayList<IrValue> getOffsets() {
        return new ArrayList<>(this.offsets);
    }
}
