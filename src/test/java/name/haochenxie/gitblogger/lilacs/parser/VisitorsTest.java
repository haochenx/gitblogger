package name.haochenxie.gitblogger.lilacs.parser;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

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
import name.haochenxie.gitblogger.lilacs.ast.Tag;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsLexer;
import name.haochenxie.gitblogger.lilacs.syntax.LilacsParser;

public class VisitorsTest {

    private Visitors visitors = new Visitors();

    @Test
    public void testConstants() {
        ImmutableMap<String, Constant> samples = ImmutableMap.<String, Constant>builder()
            .put("#t", Constant.TRUE)
            .put("#f", Constant.FALSE)
            .put("#null", Constant.NULL)
            .put("#undefined", Constant.UNDEFINED)
            .build();

        for (Entry<String, Constant> entry : samples.entrySet()) {
            String src = entry.getKey();
            Constant expected = entry.getValue();

            Exp result = parse(src).exp().accept(visitors.expVisitor);
            assertThat(String.format("Failed on '%s'", src), result, is(expected));
        }

    }

    @Test
    public void testLitString() {
        List<String> samples = Arrays.asList(
                "abc",
                "acb\\'");

        for (String entry : samples) {
            String src = String.format("\"%s\"", entry);
            String expected = src;

            LitString actual = parseAsExp(src, LitString.class);
            assertThat(String.format("Failed on '%s'", src), actual.lit(), is(expected));
        }
    }

    @Test
    public void testNumber() {
        List<String> samples = Arrays.asList(
                "10", "0.3", "0", "+3", "-10",
                "0.3", "10.", "1e17", ".88e0",
                "0039", "0x1f", "0xF3",
                "NaN", "Infinity", "-Infinity", "+NaN",
                "0");

        for (String entry : samples) {
            String src = entry;
            String expected = src;

            Number actual = parseAsExp(src, Number.class);
            assertThat(String.format("Failed on '%s'", src), actual.lit(), is(expected));
        }
    }

    @Test
    public void testInvkSexp() {
        List<String> samplesA = Arrays.asList(
                "(#null 1 2 3)", "(+ 3 4)");

        for (String entry : samplesA) {
            String src = entry;
            parseAsExp(src, InvkSexp.class);
        }

        String sampleB = "(#null 3 (=> #undefined) foo)";
        InvkSexpFunc actualB = parseAsExp(sampleB, InvkSexpFunc.class);

        assertThat(actualB.func(), is(Constant.NULL));

        String sampleC = "((x y z => x) #t #f #null)";
        InvkSexpFunc actualC = parseAsExp(sampleC, InvkSexpFunc.class);

        assertThat(actualC.args(), contains(Constant.TRUE, Constant.FALSE, Constant.NULL));

        String sampleD = "(&& #t #f)";
        InvkSexpOp actualD = parseAsExp(sampleD, InvkSexpOp.class);

        assertThat(actualD.args(), contains(Constant.TRUE, Constant.FALSE));
    }

    @Test
    public void testObjCons() {
        List<String> samplesA = Arrays.asList(
                "{}", "{ :a 1 :xml:lang lang }");

        for (String entry : samplesA) {
            String src = entry;
            parseAsExp(src, ObjCons.class);
        }

        String sample = "{ :a 1 :b:b (=> #null) }";
        ObjCons actual = parseAsExp(sample, ObjCons.class);

        assertThat(actual.fields().keySet(), containsInAnyOrder(Tag.of("a"), Tag.of("b:b")));
    }

    @Test
    public void testArrCons() {
        List<String> samplesA = Arrays.asList(
                "[]", "[a 1 #f {}/bcd (=> #null)]");

        for (String entry : samplesA) {
            String src = entry;
            parseAsExp(src, ArrCons.class);
        }

        String sample = "[#t #f #null]";
        ArrCons actual = parseAsExp(sample, ArrCons.class);

        assertThat(actual.elems(), contains(Constant.TRUE, Constant.FALSE, Constant.NULL));
    }

    @Test
    public void testIoExp() {
        String src = "(x => #undefined)/y";
        IoExp actual = parseAsExp(src, IoExp.class);

        assertThat(actual.id(), is(Id.of("y")));
        assertThat(actual.head().get(), instanceOf(LambdaSexp.class));
    }

    @Test
    public void testLambdaSexp() {
        String src = "(x y => #undefined #null)";
        Exp result = parse(src).exp().accept(visitors.expVisitor);

        assertThat(result, instanceOf(LambdaSexp.class));
        LambdaSexp casted = (LambdaSexp) result;

        assertThat(casted.args(), contains(Id.of("x"), Id.of("y")));
        assertThat(casted.bodies(), contains(Constant.UNDEFINED, Constant.NULL));
    }

    private LilacsParser parse(String src) {
        ANTLRInputStream stream = new ANTLRInputStream(src);
        LilacsLexer lexer = new LilacsLexer(stream);
        LilacsParser parser = new LilacsParser(new CommonTokenStream(lexer));
        return parser;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseAsExp(String src, Class<T> clz) {
        Exp exp = parse(src).exp().accept(visitors.expVisitor);
        assertThat(exp, instanceOf(clz));
        return (T) exp;
    }

}
