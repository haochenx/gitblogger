package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Optional;

public interface IoExp extends Exp {

    public Optional<Exp> head();

    public Id id();

    public static IoExp of(Optional<Exp> head, Id id) {
        return new IoExp() {

            @Override
            public Optional<Exp> head() {
                return head;
            }

            @Override
            public Id id() {
                return id;
            }

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitIoExp(this);
            }

        };
    }

    public static IoExp of(Id id) {
        return of(Optional.empty(), id);
    }

    public static IoExp of(Exp head, Id id) {
        return of(Optional.of(head), id);
    }

}
