package frontend;

public enum ErrorType {
    // 非法符号
    // 出现了'&'和'|'这两个符号，应该将其当做'&&'与'||'进行处理，但在记录出错行的标号仍记录'&'和'|'，报错行为'&'或'|'所在的行号。
    ILLEGAL_SYMBOL("a"),

    // 名字重定义
    // 函数或者变量名在当前作用域下重定义。注意，变量一定是同一级作用域下才会判定出错，不同级作用域内，内层会覆盖外层定义。报错行为 Ident 所在行数。
    NAME_REDEFINED("b"),

    // 未定义的名字
    // 使用了未定义的标识符号报错行为为 Ident 所在行数。
    UNDEFINED_NAME("c"),

    // 函数参数个数不匹配
    // 函数调用语句中，参数个数与函数定义中的参数个数不匹配。报错行为为函数调用语句的函数名字所在行数。
    PARAMETER_COUNT_MISMATCH("d"),

    // 函数参数类型不匹配
    // 函数调用语句中，参数类型与函数定义中对应位置的参数类型不匹配。报错行为为函数调用语句的函数名字所在行数。
    PARAMETER_TYPE_MISMATCH("e"),

    // 无返回值函数存在不匹配 return 语句
    // 报错行为 'return' 所在行号。
    VOID_RETURN_MISMATCH("f"),

    // 有返回值的函数缺少 return 语句
    // 只需要考虑函数末尾是否存在 return 语句，无需考虑数据流。报错行为为函数结尾的 '}' 所在行号。
    MISSING_RETURN("g"),

    // 不能改变常量的值
    // LVal 为常量时，不能对其修改。报错行为为 LVal 所在行数。
    MODIFY_CONSTANT("h"),

    // 缺少分号
    // 报错行为为分号前一个非终结符所在行号。
    MISSING_SEMICOLON("i"),

    // 缺少右小括号 ')'
    // 报错行为为右括号前一个非终结符所在行号。
    MISSING_RIGHT_PARENTHESIS("j"),

    // 缺少右中括号 ']'
    // 报错行为为右中括号前一个非终结符所在行号。
    MISSING_RIGHT_BRACKET("k"),

    // printf 中格式字符串与表达式个数不匹配
    // 报错行为为 'printf' 所在行号。
    PRINTF_MISMATCH("l"),

    // 在非循环块中使用 break 和 continue
    // 报错行为为 'break' 与 'continue' 所在行号。
    BREAK_CONTINUE_IN_NON_LOOP("m");

    private final String code;

    ErrorType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
