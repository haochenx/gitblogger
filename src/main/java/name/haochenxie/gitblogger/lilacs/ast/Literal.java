package name.haochenxie.gitblogger.lilacs.ast;

public interface Literal extends Exp {

    public String lit();

    @Override
    public default <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

}
