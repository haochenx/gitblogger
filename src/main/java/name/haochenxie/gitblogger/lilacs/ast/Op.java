package name.haochenxie.gitblogger.lilacs.ast;

// TODO incomplete
public enum Op {

    PLUS("+"), MINUS("-"), ASTERISK("*"), SLASH("/"),
    DAND("&&"), DOR("||"),
    LESS("<"), MORE(">"), ELESS("<="), EMORE(">="),
    BANG("!"),
    EQ("eq?"), EQQ("eq??"), NEQ("neq?"), NEQQ("neq??"),
    LARROW("<-", "="),

    ;

    public final String lit;

    public final String js;

    private Op(String lit) {
        this(lit, lit);
    }

    private Op(String lit, String js) {
        this.lit = lit;
        this.js = js;
    }

}
