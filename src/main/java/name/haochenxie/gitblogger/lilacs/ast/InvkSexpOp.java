package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Collection;

public interface InvkSexpOp extends InvkSexp {

    public Op op();

    public Collection<Exp> args();

    public static InvkSexpOp of(Op op, Collection<Exp> args) {
        return new InvkSexpOp() {

            @Override
            public Op op() {
                return op;
            }

            @Override
            public Collection<Exp> args() {
                return args;
            }

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitInvkSexpOp(this);
            }
        };
    }

}
