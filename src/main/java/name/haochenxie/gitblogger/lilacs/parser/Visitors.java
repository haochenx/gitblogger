package name.haochenxie.gitblogger.lilacs.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.tuple.ImmutablePair;

import name.haochenxie.gitblogger.lilacs.ast.ArrCons;
import name.haochenxie.gitblogger.lilacs.ast.Constant;
import name.haochenxie.gitblogger.lilacs.ast.Exp;
import name.haochenxie.gitblogger.lilacs.ast.Id;
import name.haochenxie.gitblogger.lilacs.ast.InvkSexp;
import name.haochenxie.gitblogger.lilacs.ast.InvkSexpFunc;
import name.haochenxie.gitblogger.lilacs.ast.InvkSexpOp;
import name.haochenxie.gitblogger.lilacs.ast.IoExp;
import name.haochenxie.gitblogger.lilacs.ast.LambdaSexp;
import name.haochenxie.gitblogger.lilacs.ast.LitString;
import name.haochenxie.gitblogger.lilacs.ast.Number;
import name.haochenxie.gitblogger.lilacs.ast.ObjCons;
import name.haochenxie.gitblogger.lilacs.ast.Op;
import name.haochenxie.gitblogger.lilacs.ast.RawString;
import name.haochenxie.gitblogger.lilacs.ast.SfAat;
import name.haochenxie.gitblogger.lilacs.ast.SfExact;
import name.haochenxie.gitblogger.lilacs.ast.SfIf;
import name.haochenxie.gitblogger.lilacs.ast.SfNew;
import name.haochenxie.gitblogger.lilacs.ast.SugarBegin;
import name.haochenxie.gitblogger.lilacs.ast.SugarFor;
import name.haochenxie.gitblogger.lilacs.ast.SugarForin;
import name.haochenxie.gitblogger.lilacs.ast.SugarLet;
import name.haochenxie.gitblogger.lilacs.ast.SugarShat;
import name.haochenxie.gitblogger.lilacs.ast.Tag;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsBaseVisitor;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsLexer;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.F_sexpContext;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.IoexpContext;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsVisitor;

public class Visitors {

    public final LilacsVisitor<Id> idVisitor = new LilacsBaseVisitor<Id>() {

        public Id visitId(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.IdContext ctx) {
            return Id.of(ctx.getText());
        };

    };

    public final LilacsVisitor<Deque<Id>> idsVisitor = new LilacsBaseVisitor<Deque<Id>>() {

        public Deque<Id> visitIds(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.IdsContext ctx) {
            Deque<Id> deque = ctx.ids() != null ? ctx.ids().accept(this) : new LinkedList<>();
            deque.addFirst(ctx.id().accept(idVisitor));
            return deque;
        };

    };

    public final LilacsVisitor<Op> opVisitor = new LilacsBaseVisitor<Op>() {

        public Op visitOp(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.OpContext ctx) {
            String lit = ctx.getText();
            for (Op op : Op.values()) {
                if (op.lit.equals(lit)) {
                    return op;
                }
            }

            throw new ParsingError("Unsupported (yet) op: " + lit);
        }

    };

    public final LilacsVisitor<Map<Tag, Exp>> objConsKvlVisitor = new LilacsBaseVisitor<Map<Tag, Exp>>() {

        public Map<Tag, Exp> visitObj_cons_kvl(
                name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Obj_cons_kvlContext ctx) {
            Map<Tag, Exp> map = ctx.obj_cons_kvl() != null ? ctx.obj_cons_kvl().accept(this) : new HashMap<>();
            String taglit = ctx.TAG().getText();
            if (! taglit.startsWith(":")) { throw new ParsingError("Invalid tag lit: " + taglit); }
            Tag tag = new Tag(taglit.substring(1));
            Exp exp = ctx.exp().accept(expVisitor);
            map.put(tag, exp);
            return map;
        }

    };

    public final LilacsVisitor<Map<Id, Exp>> bindsVisitor = new LilacsBaseVisitor<Map<Id, Exp>>() {

        public Map<Id, Exp> visitBinds(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.BindsContext ctx) {
            Map<Id, Exp> map = ctx.binds() != null ? ctx.binds().accept(this) : new HashMap<>();
            Id id = ctx.bind().id().accept(idVisitor);
            Exp exp = ctx.bind().exp().accept(expVisitor);
            map.put(id, exp);
            return map;
        }

    };


    public final LilacsVisitor<Exp> expVisitor = new LilacsBaseVisitor<Exp>() {

        @Override
        public IoExp visitIoexp(IoexpContext ctx) {
            Optional<ParseTree> head = Optional.<ParseTree>ofNullable(ctx.head != null ? ctx.head : ctx.headx);
            return IoExp.of(head.map(pt -> pt.accept(this)), ctx.id().accept(idVisitor));
        }

        @Override
        public LambdaSexp visitF_sexp(F_sexpContext ctx) {
            Optional<Id> name = Optional.ofNullable(ctx.name).map(n -> n.accept(idVisitor));
            Collection<Id> args = ctx.ids() != null ? ctx.ids().accept(idsVisitor) : Collections.emptyList();
            Deque<Exp> bodies = ctx.exps().accept(expsVisitor);
            return LambdaSexp.of(name, args, bodies);
        }

        public InvkSexp visitSexp_invk(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sexp_invkContext ctx) {
            Collection<Exp> args = Optional.ofNullable(ctx.exps()).<Collection<Exp>>map(a -> a.accept(expsVisitor))
                    .orElse(Collections.<Exp>emptyList());
            if (ctx.exp() != null) {
                return InvkSexpFunc.of(ctx.exp().accept(this), args);
            } else {
                return InvkSexpOp.of(ctx.op().accept(opVisitor), args);
            }
        }

        public ObjCons visitObj_cons(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Obj_consContext ctx) {
            Map<Tag, Exp> fields = ctx.obj_cons_kvl() != null ? ctx.obj_cons_kvl().accept(objConsKvlVisitor)
                    : Collections.emptyMap();
            return ObjCons.of(fields);
        }

        public ArrCons visitArr_cons(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Arr_consContext ctx) {
            Collection<Exp> exps = ctx.exps() != null ? ctx.exps().accept(expsVisitor) : Collections.emptyList();
            return ArrCons.of(exps);
        }

        public SfIf visitSf_if(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_ifContext ctx) {
            return SfIf.of(ctx.cond.accept(this), ctx.then.accept(this), ctx.els.accept(this));
        };

        public SfNew visitSf_new(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_newContext ctx) {
            Exp cons = ctx.cons.accept(this);
            Deque<Exp> args = ctx.exps().accept(expsVisitor);
            return SfNew.of(cons, args);
        }

        public SfAat visitSf_aat(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_aatContext ctx) {
            return SfAat.of(ctx.subj.accept(this), ctx.key.accept(this));
        }

        public Exp visitSf_case(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_caseContext ctx) {
            throw new ParsingError("Not supported yet");
        };

        public Exp visitSf_with(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_withContext ctx) {
            throw new ParsingError("Not supported yet");
        };

        public Exp visitSf_throw(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_throwContext ctx) {
            throw new ParsingError("Not supported yet");
        };

        public SugarLet visitSugar_let(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sugar_letContext ctx) {
            Map<Id, Exp> binds = ctx.binds().accept(bindsVisitor);
            Deque<Exp> bodies = ctx.exps().accept(expsVisitor);
            return SugarLet.of(binds, bodies);
        };

        public SugarFor visitSugar_for(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sugar_forContext ctx) {
            Entry<Id, Exp> bind = new ImmutablePair<>(ctx.bind().id().accept(idVisitor), ctx.bind().exp().accept(this));
            Deque<Exp> bodies = ctx.exps().accept(expsVisitor);
            return SugarFor.of(bind, bodies);
        }

        public SugarForin visitSugar_forin(
                name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sugar_forinContext ctx) {
            Deque<Exp> bodies = ctx.exps().accept(expsVisitor);
            return SugarForin.of(
                    ctx.keybind.accept(idVisitor),
                    ctx.valbind.accept(idVisitor),
                    ctx.obj.accept(this), bodies);
        };

        public SugarBegin visitSugar_begin(
                name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sugar_beginContext ctx) {
            return SugarBegin.of(ctx.exps().accept(expsVisitor));
        };

        public SugarShat visitSugar_shat(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sugar_shatContext ctx) {
            Id name = ctx.id().accept(idVisitor);
            Optional<Exp> ns = ctx.exp() != null ? Optional.of(ctx.exp().accept(this)) : Optional.empty();
            Map<Tag, Exp> opts = ctx.obj_cons_kvl() != null ? ctx.obj_cons_kvl().accept(objConsKvlVisitor)
                    : Collections.emptyMap();
            Deque<Exp> elems = ctx.exps().accept(expsVisitor);
            return SugarShat.of(ns, name, opts, elems);
        }

        public Constant visitConstant(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.ConstantContext ctx) {
            Token start = ctx.start;

            switch (start.getType()) {
            case LilacsLexer.C_TRUE:
                return Constant.TRUE;
            case LilacsLexer.C_FALSE:
                return Constant.FALSE;
            case LilacsLexer.C_UNDEFINED:
                return Constant.UNDEFINED;
            case LilacsLexer.C_NULL:
                return Constant.NULL;
            default:
                throw new ParsingError("Unknown constant");
            }
        };

        public LitString visitString_quoted(
                name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.String_quotedContext ctx) {
            return LitString.of(ctx.getText());
        };

        public RawString visitString_raw(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.String_rawContext ctx) {
            throw new ParsingError("Not supported yet");
        };

        public Number visitNumber(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.NumberContext ctx) {
            return Number.of(ctx.getText());
        };

        public SfExact visitSf_exact(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.Sf_exactContext ctx) {
            return SfExact.of(ctx.exp().accept(this));
        };

    };

    public final LilacsVisitor<Deque<Exp>> expsVisitor = new LilacsBaseVisitor<Deque<Exp>>() {

        public java.util.Deque<Exp> visitExps(name.haochenxie.gitblogger.lilacs.syntax.LilacsParser.ExpsContext ctx) {
            Deque<Exp> deque = ctx.exps() != null ? ctx.exps().accept(this) : new LinkedList<>();
            deque.addFirst(ctx.exp().accept(expVisitor));
            return deque;
        };

    };


}
