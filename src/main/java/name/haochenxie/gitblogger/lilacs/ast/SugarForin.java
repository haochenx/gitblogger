package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;

public interface SugarForin extends SugarSexp {

    public Id keyBind();

    public Id valBind();

    public Exp obj();

    public Collection<Exp> bodies();

    public static SugarForin of(Id keyBind, Id valBind, Exp obj, Collection<Exp> bodies) {
        return new SugarForin() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitSugarForin(this);
            }

            @Override
            public Collection<Exp> bodies() {
                return bodies;
            }

            @Override
            public Id keyBind() {
                return keyBind;
            }

            @Override
            public Id valBind() {
                return valBind;
            }

            @Override
            public Exp obj() {
                return obj;
            }

        };
    }


}
