package name.haochenxie.gitblogger.lilacs.ast;

public interface SfAat extends SfSexp {

    public Exp subj();

    public Exp key();

    public static SfAat of(Exp subj, Exp key) {
        return new SfAat() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSfAat(this);
            }

            @Override
            public Exp subj() {
                return subj;
            }

            @Override
            public Exp key() {
                return key;
            }

        };
    }

}
