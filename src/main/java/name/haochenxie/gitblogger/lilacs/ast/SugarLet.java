package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;
import java.util.Map;

public interface SugarLet extends SugarSexp {

    public Map<Id, Exp> binds();

    public Collection<Exp> bodies();

    public static SugarLet of(Map<Id, Exp> binds, Collection<Exp> bodies) {
        return new SugarLet() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSugarLet(this);
            }

            @Override
            public Collection<Exp> bodies() {
                return bodies;
            }

            @Override
            public Map<Id, Exp> binds() {
                return binds;
            }
        };
    }

}
