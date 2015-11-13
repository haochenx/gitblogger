package name.haochenxie.gitblogger.framework.util;

import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Preconditions;

// TODO not complete
public class SuperOptional<T> {

    @FunctionalInterface
    public interface CheckedFunction<T,R> {

        public R apply(T arg) throws Exception;

    }

    private final static SuperOptional<?> EMPTY = new SuperOptional<>();

    private T value;

    private SuperOptional() { }

    public SuperOptional(T value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public static <T> SuperOptional<T> empty() {
        @SuppressWarnings("unchecked")
        SuperOptional<T> empty = (SuperOptional<T>) EMPTY;
        return empty;
    }

    public static <T> SuperOptional<T> of(T val) {
        return new SuperOptional<T>(val);
    }

    public static <T> SuperOptional<T> ofNullable(T val) {
        if (val == null) {
            return empty();
        } else {
            return of(val);
        }
    }

    public <U> SuperOptional<U> failableMap(CheckedFunction<? super T, ? extends U> func) {
        try {
            return ofNullable(func.apply(this.value));
        } catch (Throwable ex) {
            return empty();
        }
    }

    public T orElse(T or) {
        return value == null ? or : value;
    }

    public <U> SuperOptional<U> map(Function<? super T, ? extends U> func) {
        return ofNullable(func.apply(this.value));
    }

    public static <T> SuperOptional<T> from(Optional<T> op) {
        if (op.isPresent()) {
            return new SuperOptional<>(op.get());
        } else {
            return empty();
        }
    }

}
