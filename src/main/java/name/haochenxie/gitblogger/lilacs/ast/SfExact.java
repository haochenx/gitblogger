package name.haochenxie.gitblogger.lilacs.ast;

public interface SfExact extends SfSexp {

    public Exp exp();

    public static SfExact of(Exp exp) {
        return new SfExact() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSfExact(this);
            }

            @Override
            public Exp exp() {
                return exp;
            }
        };
    }

}
