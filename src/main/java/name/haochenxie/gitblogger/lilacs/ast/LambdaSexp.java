package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public interface LambdaSexp extends Exp {

    public Optional<Id> name();

    public Collection<Id> args();

    public Collection<Exp> bodies();

    public static LambdaSexp of(Optional<Id> name, Collection<Id> args, Collection<Exp> bodies) {
        return new LambdaSexp() {

            @Override
            public Optional<Id> name() {
                return name;
            }

            @Override
            public Collection<Exp> bodies() {
                return bodies;
            }

            @Override
            public Collection<Id> args() {
                return args;
            }

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitLambdaSexp(this);
            }

        };
    }

    public static LambdaSexp of(Id arg, Collection<Exp> bodies) {
        return of(Optional.empty(), Arrays.asList(arg), bodies);
    }

    public static LambdaSexp of(Id arg, Exp body) {
        return of(arg, Arrays.asList(body));
    }

}
