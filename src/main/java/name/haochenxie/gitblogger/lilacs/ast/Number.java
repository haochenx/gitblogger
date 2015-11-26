package name.haochenxie.gitblogger.lilacs.ast;

public interface Number extends Literal {

    public static Number of(String lit) {
        return new Number() {

            @Override
            public String lit() {
                return lit;
            }

        };
    }

}
