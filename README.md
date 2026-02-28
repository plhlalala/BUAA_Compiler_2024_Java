# BUAA 编译原理 2024 编译器（Java 实现）

本项目是北京航空航天大学 2024 年编译原理课程的大作业，使用 Java 实现了一个完整的编译器，能够将类 C 语言（SysY 子集）源程序编译为 MIPS 汇编代码。

## 项目结构

```
.
├── Compiler.java              # 编译器入口
├── frontend/                  # 前端
│   ├── lexer/                 # 词法分析
│   │   ├── Lexer.java         # 词法分析器
│   │   ├── Token.java         # Token 类
│   │   └── LexType.java       # Token 类型枚举
│   ├── parser/                # 语法分析
│   │   ├── Parser.java        # 递归下降语法分析器
│   │   └── TokenBuf.java      # Token 缓冲区
│   ├── symtable/              # 符号表
│   │   ├── SymTable.java      # 符号表
│   │   ├── Symbol.java        # 符号基类
│   │   ├── FuncSym.java       # 函数符号
│   │   ├── VarSym.java        # 变量符号
│   │   └── VarType.java       # 变量类型枚举
│   ├── visitor_Symtable/      # 语义分析（符号表构建与错误检查）
│   │   ├── Visitor_Symtable.java
│   │   └── VisitResult.java
│   └── error/                 # 错误处理
│       ├── ErrorRecord.java   # 错误记录
│       └── ErrorType.java     # 错误类型枚举
├── middleend/                 # 中间端（LLVM IR 生成）
│   ├── Visitor_IR.java        # IR 生成访问者
│   ├── LLVM_components/       # LLVM IR 数据结构
│   │   ├── IrModule.java      # 模块
│   │   ├── Function.java      # 函数
│   │   ├── BasicBlock.java    # 基本块
│   │   ├── IrValue.java       # IR 值基类
│   │   ├── User.java          # Use-Def 链 User
│   │   ├── Use.java           # Use-Def 链 Use
│   │   └── ...
│   ├── instruction/           # LLVM 指令
│   │   ├── AllocaInstr.java   # alloca
│   │   ├── BinaryInstr.java   # 二元运算
│   │   ├── BrInstr.java       # 跳转
│   │   ├── CallInstr.java     # 函数调用
│   │   ├── GetelementptrInstr.java  # 地址计算
│   │   ├── IcmpInstr.java     # 整数比较
│   │   ├── LoadInstr.java     # 内存读
│   │   ├── StoreInstr.java    # 内存写
│   │   ├── PhiInstr.java      # Phi 指令
│   │   ├── ReturnInstr.java   # 返回
│   │   └── ...
│   └── type/                  # LLVM 类型系统
│       ├── LLVMType.java
│       ├── BasicType.java
│       └── ArrayType.java
├── optimize/                  # 中间端优化
│   ├── OptimizationPipeline.java  # 优化流水线
│   ├── BrOptimize.java        # 跳转优化
│   ├── DeadCodeRemove.java    # 死代码删除
│   ├── BlockSimplify.java     # 基本块化简
│   ├── CFG.java               # 控制流图构建
│   ├── Mem2Reg.java           # mem2reg（构建 SSA）
│   ├── PhiRemove.java         # Phi 指令消除（退出 SSA）
│   ├── LiveVariableAnalysis.java  # 活跃变量分析
│   └── Allocator.java         # 寄存器分配
└── backend/                   # 后端（MIPS 代码生成）
    ├── Generator.java         # MIPS 代码生成器
    ├── MIPSmodule.java        # MIPS 模块
    ├── ValueManager.java      # 值到寄存器/内存的映射
    ├── Data/                  # .data 段
    │   ├── Data.java
    │   └── DataType.java
    ├── Text/                  # .text 段
    │   ├── InstrText.java     # MIPS 指令
    │   ├── LabelText.java     # 标签
    │   └── Text.java          # 文本基类
    └── Value/                 # MIPS 操作数
        ├── MIPSRegister.java  # 寄存器
        ├── MIPSImmediate.java # 立即数
        ├── MIPSOffset.java    # 偏移寻址
        ├── MIPSLabel.java     # 标签引用
        └── MIPSValue.java     # 操作数基类
```

## 编译器功能

### 支持的语言特性

- **数据类型**：`int`、`char`、`void`，以及一维/二维数组
- **常量与变量**：`const` 修饰的常量声明，普通变量声明与初始化
- **运算符**：算术运算（`+`、`-`、`*`、`/`、`%`）、关系运算（`<`、`>`、`<=`、`>=`、`==`、`!=`）、逻辑运算（`&&`、`||`、`!`）
- **控制流**：`if`/`else`、`for` 循环、`break`、`continue`、`return`
- **函数**：函数定义与调用，支持递归
- **输入输出**：`getint()`、`getchar()`、`printf()`

### 错误处理

词法、语法和语义阶段均进行错误检测，支持的错误类型如下：

| 错误码 | 错误类型 |
|--------|----------|
| `a` | 非法符号（`&`、`\|` 未成对出现） |
| `b` | 名字重定义 |
| `c` | 未定义的名字 |
| `d` | 函数参数个数不匹配 |
| `e` | 函数参数类型不匹配 |
| `f` | 无返回值函数存在不匹配的 `return` 语句 |
| `g` | 有返回值的函数缺少 `return` 语句 |
| `h` | 不能改变常量的值 |
| `i` | 缺少分号 |
| `j` | 缺少右小括号 `)` |
| `k` | 缺少右中括号 `]` |
| `l` | `printf` 中格式字符串与表达式个数不匹配 |
| `m` | 在非循环块中使用 `break`/`continue` |

### 优化流水线

编译器在生成 MIPS 代码前对 LLVM IR 执行以下优化（可通过 `Compiler.java` 中的 `optimize` 开关控制）：

1. **跳转优化**（BrOptimize）：化简冗余跳转
2. **死代码删除**（DeadCodeRemove）：删除不可达指令
3. **基本块化简**（BlockSimplify）：合并可合并的基本块
4. **控制流图构建**（CFG）：计算支配树等信息
5. **Mem2Reg**：将 `alloca`/`load`/`store` 提升为 SSA 形式的寄存器
6. **Phi 指令消除**（PhiRemove）：退出 SSA，插入并行拷贝
7. **活跃变量分析**（LiveVariableAnalysis）：为寄存器分配提供信息
8. **寄存器分配**（Allocator）：将虚拟寄存器映射到 MIPS 物理寄存器

## 编译与运行

### 环境要求

- JDK 17 或以上

### 编译

```bash
javac -encoding UTF-8 -d out $(find . -name "*.java" | grep -v "\.git")
```

### 运行

将待编译的源文件命名为 `testfile.txt` 放于项目根目录，然后执行：

```bash
java -cp out Compiler
```

运行后将在当前目录生成以下文件：

| 文件 | 说明 |
|------|------|
| `parser.txt` | 词法与语法分析输出 |
| `symbol.txt` | 符号表输出 |
| `error.txt` | 错误信息输出（有错误时生成） |
| `llvm_ir.txt` | 生成的 LLVM IR（无错误时生成） |
| `mips.txt` | 生成的 MIPS 汇编代码（无错误时生成） |

## 编译流程

```
源程序 (testfile.txt)
        │
        ▼
  ┌─────────────┐
  │  词法分析    │  Lexer → Token 序列
  └─────────────┘
        │
        ▼
  ┌─────────────┐
  │  语法分析    │  递归下降 → AST（CompUnit）
  └─────────────┘
        │
        ▼
  ┌─────────────┐
  │  语义分析    │  构建符号表，检查语义错误
  └─────────────┘
        │（无错误）
        ▼
  ┌─────────────┐
  │  IR 生成     │  遍历 AST → LLVM IR
  └─────────────┘
        │
        ▼
  ┌─────────────┐
  │  IR 优化     │  可选，见优化流水线
  └─────────────┘
        │
        ▼
  ┌─────────────┐
  │  代码生成    │  LLVM IR → MIPS 汇编
  └─────────────┘
        │
        ▼
  MIPS 汇编 (mips.txt)
```

## 课程信息

- **学校**：北京航空航天大学（BUAA）
- **课程**：编译原理
- **年份**：2024
