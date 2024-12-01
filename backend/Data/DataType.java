package backend.Data;

public enum DataType {
    BYTE, WORD, SPACE, ASCIIZ, ASCII;

    @Override
    public String toString() {
        return switch (this) {
            case BYTE -> ".byte";
            case WORD -> ".word";
            case SPACE -> ".space";
            case ASCIIZ -> ".asciiz";
            case ASCII -> ".ascii";
            default -> throw new RuntimeException("Unknown data type");
        };
    }
}

//.data
//
//        # 声明 32 位整数
//int1:  .word 10
//int2:  .word 1, 2, 3
//        # 声明 8 位整数
//byte1: .byte 255
//byte2: .byte 1, 2, 3, 4
//        # 声明字符串
//str1:  .asciiz "Hello, MIPS!"   # 包含 \0 结尾
//str2:  .ascii "No Null End"     # 不包含 \0 结尾
//        # 声明未初始化空间
//array:  .space 40              # 分配 40 字节未初始化空间
//# 数据对齐
//        .align 2
//aligned_word: .word 5          # 对齐到 4 字节边界
//        .align 3
//aligned_double: .double 3.14   # 对齐到 8 字节边界

