package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;

public interface ArrCons extends Exp {

    public Collection<Exp> elems();

    public static ArrCons of(Collection<Exp> elems) {
        return new ArrCons() {

            @Override
            public Collection<Exp> elems() {
                return elems;
            }

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitArrCons(this);
            }

        };
    }

}
