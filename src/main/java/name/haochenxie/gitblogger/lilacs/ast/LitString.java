package name.haochenxie.gitblogger.lilacs.ast;

public interface LitString extends Literal {

    public static LitString of(String lit) {
        return new LitString() {

            @Override
            public String lit() {
                return lit;
            }
        };
    }

}
