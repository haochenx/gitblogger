package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;
import java.util.Map.Entry;

public interface SugarFor extends SugarSexp {

    public Entry<Id, Exp> bind();

    public Collection<Exp> bodies();

    public static SugarFor of(Entry<Id, Exp> bind, Collection<Exp> bodies) {
        return new SugarFor() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSugarFor(this);
            }

            @Override
            public Collection<Exp> bodies() {
                return bodies;
            }

            @Override
            public Entry<Id, Exp> bind() {
                return bind;
            }
        };
    }

}
