package name.haochenxie.gitblogger.lilacs.ast;

public interface Exp {

    public <T> T accept(ASTVisitor<T> visitor);

}
