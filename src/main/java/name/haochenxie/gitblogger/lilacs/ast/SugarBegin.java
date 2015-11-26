package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;

public interface SugarBegin extends SugarSexp {

    public Collection<Exp> bodies();

    public static SugarBegin of(Collection<Exp> bodies) {
        return new SugarBegin() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSugarBegin(this);
            }

            @Override
            public Collection<Exp> bodies() {
                return bodies;
            }
        };
    }

}
