package name.haochenxie.gitblogger.lilacs;

import com.google.common.base.Suppliers;
import name.haochenxie.gitblogger.lilacs.translator.LilacsJavascriptTranslator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

import static name.haochenxie.gitblogger.utilities.FunctionalFacilities.wrapOptional;

public class LilacsRepl {

    private static final Function<String, String> lilacsCompiler = LilacsJavascriptTranslator::compile;

    @SuppressWarnings("unused")
    public enum JavaScriptLibrary {

        UNDERSCORE("Underscore.js", "/javascript/underscore-min.1.8.3.js"),

        ;

        public final String desc;
        public final Supplier<Optional<String>> code;

        private JavaScriptLibrary(String desc, String resPath) {
            this(desc, resPath, Function.identity());
        }

        private JavaScriptLibrary(String desc, String resPath, Function<String, String> converter) {
            this.desc = desc;
            this.code = () ->
                    Optional.ofNullable(LilacsRepl.class.getResourceAsStream(resPath))
                            .flatMap(wrapOptional(IOUtils::toString))
                            .map(converter);
        }

    }

    @SuppressWarnings("unused")
    public static class API {

        private ScriptEngine engine;

        public API(ScriptEngine engine) {
            this.engine = engine;
        }

        public boolean loadJavaScriptLibrary(String path) {
            return loadLibrary(path, Function.identity());
        }

        public boolean loadLilacsLibrary(String path) {
            return loadLibrary(path, lilacsCompiler);
        }

        public String compileLilacs(String lilacs) {
            try {
                return lilacsCompiler.apply(lilacs);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private boolean loadLibrary(String path, Function<String, String> converter) {
            final boolean[] loaded = {false};
            Optional.of(new File(path))
                    .flatMap(wrapOptional(FileUtils::readFileToString, System.err))
                    .ifPresent(code -> {
                        try {
                            engine.eval(converter.apply(code));
                            System.err.println("Library loaded: " + path);
                            loaded[0] = true;
                        } catch (ScriptException e) {
                            System.err.println("Library loading failed: " + path);
                            e.printStackTrace();
                        }
                    });
            return loaded[0];
        }

    }

    private static com.google.common.base.Supplier<Scanner> scannerSupplier =
            Suppliers.memoize(() -> new Scanner(System.in));

    private static String readLine() {
        System.out.flush();
        System.err.flush();

        if (System.console() != null) {
            return System.console().readLine();
        } else {
            return scannerSupplier.get().nextLine();
        }
    }

    public static void main(String[] args) throws Exception {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");

        API api = new API(engine);
        engine.put("api", api);

        for (JavaScriptLibrary lib : JavaScriptLibrary.values()) {
            lib.code.get()
                    .ifPresent(libCode -> {
                        try {
                            engine.eval(libCode);
                            System.err.println("Library loaded: " + lib.desc);
                        } catch (ScriptException e) {
                            System.err.println("Library loading failed: " + lib.desc);
                            e.printStackTrace();
                        }
                    });
        }

        while (true) {
            System.err.print("=> ");

            StringBuilder buff = new StringBuilder();
            String line;

            for (line = readLine();
                 line != null && !line.trim().isEmpty();
                 line = readLine()) {
                buff.append(line);
                buff.append('\n');
                System.err.print(" : ");
            }

            if (line == null) {
                System.exit(0);
            }

            try {
                String js = LilacsJavascriptTranslator.compile(buff.toString());
                Object result = engine.eval(js);
                System.out.println(result);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

}
