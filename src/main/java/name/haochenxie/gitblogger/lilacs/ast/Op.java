package name.haochenxie.gitblogger.lilacs.ast;

import static name.haochenxie.gitblogger.lilacs.ast.Op.AttributeType.*;

// TODO incomplete
/*

Lilacs provides polish style operators for the following JavaScript's binary operators with the same name:

+, -, *, /, %, >, >=, <, <=, &&, ||, &, |, ^, <<, >>, >>>

and the following with a different name:

eq? -> ==
eq?? -> ===
neq? -> !=
neq?? -> !==

and also the following unary operators with the same name:

+, -, !, ~

 */
public enum Op {

    PLUS("+", UNARY_ASSOCIATIVE),
    MINUS("-", UNARY_ASSOCIATIVE),

    ASTERISK("*", ASSOCIATIVE),
    SLASH("/", ASSOCIATIVE),
    PERCENTAGE("%", ASSOCIATIVE),

    IAND("&", ASSOCIATIVE),
    IOR("|", ASSOCIATIVE),
    IXOR("^", ASSOCIATIVE),

    DAND("&&", ASSOCIATIVE),
    DOR("||", ASSOCIATIVE),

    SHIFT_R(">>", ASSOCIATIVE),
    SHIFT_R2(">>>", ASSOCIATIVE),
    SHIFT_L("<<", ASSOCIATIVE),

    LESS("<", BINARY),
    MORE(">", BINARY),
    ELESS("<=", BINARY),
    EMORE(">=", BINARY),

    BANG("!", UNARY),
    TILDE("~", UNARY),

    EQ("eq?", BINARY, "=="),
    EQQ("eq??", BINARY, "==="),
    NEQ("neq?", BINARY, "!="),
    NEQQ("neq??", BINARY, "!=="),

    LARROW("<-", BINARY, "="),

    ;

    public enum AttributeType {

        UNARY(true, false, false),
        BINARY(false, true, false),
        UNARY_BINARY(true, true, false),
        ASSOCIATIVE(false, true, true),
        UNARY_ASSOCIATIVE(true, true, true),

        ;

        public final boolean unary;
        public final boolean binary;

        /**
         * Note: associativity implies binarity
         */
        public final boolean associative;

        AttributeType(boolean unary, boolean binary, boolean associative) {
            this.unary = unary;
            this.binary = binary;
            this.associative = associative;
        }
    }

    public final AttributeType nature;

    public final String lit;

    public final String js;

    private Op(String lit, AttributeType nature) {
        this(lit, nature, lit);
    }

    private Op(String lit, AttributeType nature, String js) {
        this.lit = lit;
        this.nature = nature;
        this.js = js;
    }

}
