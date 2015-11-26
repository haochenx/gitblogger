package name.haochenxie.gitblogger.lilacs.ast;

public interface SfIf extends SfSexp {

    public Exp cond();

    public Exp then();

    public Exp els();

    public static SfIf of(Exp cond, Exp then, Exp els) {
        return new SfIf() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSfIf(this);
            }

            @Override
            public Exp then() {
                return then;
            }

            @Override
            public Exp els() {
                return els;
            }

            @Override
            public Exp cond() {
                return cond;
            }
        };
    }

}
