package name.haochenxie.gitblogger.utilities;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Function;

public class FunctionalFacilities {

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static <T,R> Function<T, Optional<R>> wrapOptional(CheckedFunction<T, R> f) {
        return wrapOptional(f, null);
    }

    public static <T,R> Function<T, Optional<R>> wrapOptional(CheckedFunction<T, R> f, OutputStream os) {
        return v -> {
            try {
                return Optional.ofNullable(f.apply(v));
            } catch (Exception ex) {
                if (os != null) {
                    ex.printStackTrace(new PrintStream(os));
                }
                return Optional.empty();
            }
        };
    }

}
