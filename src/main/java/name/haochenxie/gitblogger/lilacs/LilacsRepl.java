package name.haochenxie.gitblogger.lilacs;

import name.haochenxie.gitblogger.lilacs.translator.LilacsJavascriptTranslator;
import org.apache.commons.io.IOUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Optional;

import static name.haochenxie.gitblogger.utilities.FunctionalFacilities.wrapOptional;

public class LilacsRepl {

    public enum JavaScriptLibrary {

        UNDERSCORE("Underscore.js", "/javascript/underscore-min.1.8.3.js"),

        ;

        public final String desc;
        public final String resPath;

        private JavaScriptLibrary(String desc, String resPath) {
            this.desc = desc;
            this.resPath = resPath;
        }

    }

    public static void main(String[] args) throws Exception {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");

        for (JavaScriptLibrary lib : JavaScriptLibrary.values()) {
            Optional.ofNullable(LilacsRepl.class.getResourceAsStream(lib.resPath))
                    .flatMap(wrapOptional(IOUtils::toString))
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

            for (line = System.console().readLine();
                 line != null && !line.trim().isEmpty();
                 line = System.console().readLine()) {
                buff.append(line);
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
