package name.haochenxie.gitblogger.lilacs.translator;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LilacsJavascriptTranslatorTest {

    @Test
    public void test() {
        List<String> samples = Arrays.asList(
                "{:b #f :abc 100.7}", "({} 1 2 3)", "({}/toString 1 2 3)",
                "((x y z => x ((=> z))) 1 2 3)", "((x y z => y) 1 2 3)", "(([rect] x y z => y) 1 2 3)",
                "(console/log [1 (=> #null) 2]/length)", "(+ 3 ((x y z => y) 1 2 3))", "(! #t)",
                "(exact [1 (=> #null) 2]/length)", "(if #t 1 2)", "([fac] n => (if (< n 1) 1 (* n (fac (- n 1)))))",
                "([fib] n => (if (<= n 2) 1 (+ (fib (- n 1)) (fib (- n 2)))))",
                String.format("(let ((fib %s) (n 7)) (fib n))",
                        "([fib] n => (if (<= n 2) 1 (+ (fib (- n 1)) (fib (- n 2)))))"),
                "(exact (new Array 1 3 2 4)/length)", "(for (x [2 1 3]) (console/log x))",
                String.format("(let ((fib %s)) (for (n [1 2 3 4 5 6 7 8 9 10]) (fib n)))",
                        "([fib] n => (if (<= n 2) 1 (+ (fib (- n 1)) (fib (- n 2)))))"),
                "(begin (console/log \"hello\") (console/log \"world!\"))", "(@@ (+ 1 2) [3 1 2 4 5 8 3])",
                "(@@ \"abc\" { :abc 11 })", "(@a :href \"http://google.com\" :: \"Google\")",
                "((require \"k\")/@a :href \"http://google.com\" :: \"Google\")",
                "(begin (<- _shat {:a (opts elems => (console/log opts) (console/log elems))}) "
                        + "(@a :href \"http://google.com\" :: \"Google\"))",
                "(for-in (k v { :a 1 :b 2 }) (+ (+ k \"=\") v))");

        for (String sample : samples) {
            printCompiledSample(sample);
        }

    }

    private void printCompiledSample(String sample) {
        String js = LilacsJavascriptTranslator.compile(sample);
        System.out.println(sample);
        System.out.println("->");
        System.out.println(js);
        System.out.println();
    }

}
