package name.haochenxie.gitblogger.lilacs.ast;

public enum Constant implements Literal {

    UNDEFINED("undefined"), NULL("null"), TRUE("true"), FALSE("false");

    private final String lit;

    private Constant(String lit) {
        this.lit = lit;
    }

    @Override
    public String lit() {
        return lit;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }

}
