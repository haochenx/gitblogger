package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Map;

public interface ObjCons extends Exp {

    public Map<Tag, Exp> fields();

    public static ObjCons of(Map<Tag, Exp> fields) {
        return new ObjCons() {

            @Override
            public Map<Tag, Exp> fields() {
                return fields;
            }

            @Override
            public <T> T accept(ASTVisitor<T> visitor) {
                return visitor.visitObjCons(this);
            }

        };
    }

}
