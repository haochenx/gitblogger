package name.haochenxie.gitblogger.lilacs.ast;

public interface RawString extends Exp {

    /**
     * post processor
     */
    public Exp post();

    public String content();

    public static RawString of(Exp post, String content) {
        return new RawString() {

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitRawString(this);
            }

            @Override
            public Exp post() {
                return post;
            }

            @Override
            public String content() {
                return content;
            }

        };
    }

}
