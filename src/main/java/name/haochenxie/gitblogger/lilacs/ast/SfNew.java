package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;

public interface SfNew extends SfSexp {

    public Exp cons();

    public Collection<Exp> args();

    public static SfNew of(Exp cons, Collection<Exp> args) {
        return new SfNew() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSfNew(this);
            }

            @Override
            public Exp cons() {
                return cons;
            }

            @Override
            public Collection<Exp> args() {
                return args;
            }

        };
    }

}
