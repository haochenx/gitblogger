package name.haochenxie.gitblogger.lilacs.ast;

public interface ASTVisitor<T> {

    T visitExp(Exp exp);

    T visitIoExp(IoExp ioExp);

    T visitArrCons(ArrCons argCons);

    T visitConstant(Constant constant);

    T visitInvkSexpFunc(InvkSexpFunc invkSexpFunc);

    T visitInvkSexpOp(InvkSexpOp invkSexpOp);

    T visitLambdaSexp(LambdaSexp lambdaSexp);

    T visitLiteral(Literal literal);

    T visitObjCons(ObjCons objCons);

    T visitSfExact(SfExact sfExact);

    T visitSfIf(SfIf sfIf);

    T visitSugarLet(SugarLet sugarLet);

    T visitSfNew(SfNew sfNew);

    T visitSugarFor(SugarFor sugarFor);

    T visitSugarBegin(SugarBegin sugarBegin);

    T visitSfAat(SfAat sfAat);

    T visitRawString(RawString rawString);

    T visitSugarShat(SugarShat sugarShat);

    T visitSugarForin(SugarForin sugarForin);

}
