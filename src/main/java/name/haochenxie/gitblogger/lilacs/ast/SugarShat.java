package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface SugarShat extends SugarSexp {

    /**
     * namespace of this shat
     */
    public Optional<Exp> ns();

    public Id name();

    public Map<Tag, Exp> opts();

    public Collection<Exp> elems();

    public static SugarShat of(Optional<Exp> ns, Id name, Map<Tag, Exp> opts, Collection<Exp> elems) {
        return new SugarShat() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSugarShat(this);
            }

            @Override
            public Map<Tag, Exp> opts() {
                return opts;
            }

            @Override
            public Optional<Exp> ns() {
                return ns;
            }

            @Override
            public Id name() {
                return name;
            }

            @Override
            public Collection<Exp> elems() {
                return elems;
            }
        };
    }

}
