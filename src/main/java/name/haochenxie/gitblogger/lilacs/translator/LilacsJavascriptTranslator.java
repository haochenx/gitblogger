package name.haochenxie.gitblogger.lilacs.translator;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import name.haochenxie.gitblogger.lilacs.ast.*;
import name.haochenxie.gitblogger.lilacs.parser.Visitors;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsLexer;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class LilacsJavascriptTranslator {

    private static LilacsParser parse(String src) {
        ANTLRInputStream stream = new ANTLRInputStream(src);
        LilacsLexer lexer = new LilacsLexer(stream);
        LilacsParser parser = new LilacsParser(new CommonTokenStream(lexer));
        return parser;
    }

    public static String translate(Exp ast) {
        return ast.accept(new TranslatorVisitor());
    }

    public static class TranslatorVisitor implements ASTVisitor<String> {

        @Override
        public String visitExp(Exp exp) {
            throw new UnsupportedOperationException("not supported yet: " + exp.getClass().getSimpleName());
        }

        @Override
        public String visitIoExp(IoExp exp) {
            String head = exp.head().map(h -> h.accept(this) + ".").orElse("");
            return head + exp.id().name();
        }

        @Override
        public String visitArrCons(ArrCons argCons) {
            List<String> elems = argCons.elems().stream().map(e -> e.accept(this)).collect(toList());
            return String.format("[%s]",
                    Joiner.on(',').join(elems));
        }

        @Override
        public String visitConstant(Constant constant) {
            return constant.lit();
        }

        @Override
        public String visitInvkSexpFunc(InvkSexpFunc exp) {
            List<String> args = exp.args().stream().map(e -> e.accept(this)).collect(toList());
            return String.format("%s(%s)", exp.func().accept(this), Joiner.on(',').join(args));
        }

        @Override
        public String visitInvkSexpOp(InvkSexpOp invkSexpOp) {
            List<String> operants = invkSexpOp.args().stream().map(e -> e.accept(this)).collect(toList());
            Op.AttributeType opNature = invkSexpOp.op().nature;
            if (operants.size() == 1 && !opNature.unary) {
                throw new IllegalArgumentException(invkSexpOp.op().lit + " is not a unary operator");
            } else if (operants.size() == 2 && !opNature.binary) {
                throw new IllegalArgumentException(invkSexpOp.op().lit + " is not a binary operator");
            } else if (operants.size() > 2 && !opNature.associative) {
                throw new IllegalArgumentException(invkSexpOp.op().lit + " cannot be used this way");
            }
            switch (operants.size()) {
            case 1:
                return String.format("%s%s", invkSexpOp.op().js, operants.get(0));
            default:
                return Joiner.on(invkSexpOp.op().js).join(operants);
            }
        }

        @Override
        public String visitLambdaSexp(LambdaSexp lambdaSexp) {
            String name = lambdaSexp.name().map(n -> " " + n.name()).orElse("");
            List<String> args = lambdaSexp.args().stream().map(id -> id.name()).collect(toList());
            List<String> bodies = lambdaSexp.bodies().stream().map(e -> e.accept(this)).collect(toList());
            return String.format("(function%s(%s){%sreturn %s;})", name,
                    Joiner.on(',').join(args),
                    Joiner.on("").join(bodies.subList(0, bodies.size() - 1).stream().map(s -> s+';').collect(toList())),
                    Iterables.getLast(bodies));
        }

        @Override
        public String visitLiteral(Literal literal) {
            return literal.lit();
        }

        @Override
        public String visitObjCons(ObjCons objCons) {
            List<String> fields = objCons.fields().entrySet().stream()
                    .map(e -> String.format("%s:%s", e.getKey().name(), e.getValue().accept(this)))
                    .collect(toList());
            return String.format("({%s})", Joiner.on(',').join(fields));
        }

        @Override
        public String visitSfExact(SfExact sfExact) {
            return sfExact.exp().accept(this);
        }

        @Override
        public String visitSfIf(SfIf sfIf) {
            return String.format("%s?%s:%s",
                    sfIf.cond().accept(this),
                    sfIf.then().accept(this),
                    sfIf.els().accept(this));
        }

        @Override
        public String visitSugarLet(SugarLet sugarLet) {
            LambdaSexp func = LambdaSexp.of(Optional.empty(), sugarLet.binds().keySet(), sugarLet.bodies());
            InvkSexpFunc invk = InvkSexpFunc.of(func, sugarLet.binds().values());
            return invk.accept(this);
        }

        @Override
        public String visitSfNew(SfNew sfNew) {
            List<String> args = sfNew.args().stream().map(e -> e.accept(this)).collect(toList());
            return String.format("new (%s)(%s)", sfNew.cons().accept(this),
                    Joiner.on(',').join(args));
        }

        @Override
        public String visitSugarFor(SugarFor sugarFor) {
            IoExp map = IoExp.of(sugarFor.bind().getValue(), Id.of("map"));
            LambdaSexp func = LambdaSexp.of(Optional.empty(), Arrays.asList(sugarFor.bind().getKey()),
                    sugarFor.bodies());
            InvkSexpFunc invk = InvkSexpFunc.of(map, Arrays.asList(func));
            return invk.accept(this);
        }

        @Override
        public String visitSugarForin(SugarForin sugarForin) {
            Id objbind = Id.of("__lilacs_obj");
            InvkSexpFunc invk = InvkSexpFunc.of(LambdaSexp.of(objbind,
                    InvkSexpFunc.of(
                            IoExp.of(InvkSexpFunc.of(IoExp.of(IoExp.of(Id.of("Object")), Id.of("keys")),
                                    IoExp.of(objbind)), Id.of("map")),
                    LambdaSexp.of(sugarForin.keyBind(),
                            Arrays.asList(InvkSexpFunc.of(LambdaSexp.of(sugarForin.valBind(), sugarForin.bodies()),
                                    Arrays.asList(SfAat.of(IoExp.of(objbind), IoExp.of(sugarForin.keyBind())))))))),
                    sugarForin.obj());
            return invk.accept(this);
        }

        @Override
        public String visitSugarBegin(SugarBegin sugarBegin) {
            InvkSexpFunc invk = InvkSexpFunc.of(
                    LambdaSexp.of(Optional.empty(), Collections.emptyList(), sugarBegin.bodies()),
                    Collections.emptyList());
            return invk.accept(this);
        }

        @Override
        public String visitSfAat(SfAat sfAat) {
            return String.format("%s[%s]", sfAat.subj().accept(this), sfAat.key().accept(this));
        }

        @Override
        public String visitRawString(RawString rawString) {
            return visitExp(rawString); // TODO
        }

        @Override
        public String visitSugarShat(SugarShat sugarShat) {
            IoExp ns = IoExp.of(sugarShat.ns(), Id.of("_shat"));
            IoExp func = IoExp.of(ns, sugarShat.name());
            ObjCons opts = ObjCons.of(sugarShat.opts());
            ArrCons elems = ArrCons.of(sugarShat.elems());
            InvkSexpFunc invk = InvkSexpFunc.of(func, Arrays.asList(opts, elems));
            return invk.accept(this);
        }

    }

    public static String compile(String lilacsSourceCode) {
        Visitors visitors = new Visitors();
        ArrayList<String> lines = new ArrayList<>();

        Deque<Exp> exps = parse(lilacsSourceCode).program().exps().accept(visitors.expsVisitor);

        for (Exp exp : exps) {
            String js = exp.accept(new TranslatorVisitor());
            lines.add(js);
        }

        return Joiner.on("\n").join(lines);
    }

    public static void main(String[] args) throws Exception {
        String in = IOUtils.toString(System.in).trim();
        System.out.println(compile(in));
    }

}
