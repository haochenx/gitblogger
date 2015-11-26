package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Arrays;
import java.util.Collection;

public interface InvkSexpFunc extends InvkSexp {

    public Exp func();

    public Collection<Exp> args();

    public static InvkSexpFunc of(Exp func, Collection<Exp> args) {
        return new InvkSexpFunc() {

            @Override
            public Exp func() {
                return func;
            }

            @Override
            public Collection<Exp> args() {
                return args;
            }

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitInvkSexpFunc(this);
            }
        };
    }

    public static InvkSexpFunc of(Exp func, Exp arg) {
        return of(func, Arrays.asList(arg));
    }

}
