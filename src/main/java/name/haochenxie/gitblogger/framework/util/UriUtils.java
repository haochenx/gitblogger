package name.haochenxie.gitblogger.framework.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

public class UriUtils {

    public static String[] combine(String part1, String[] part2) {
        return ObjectArrays.concat(canonizePath(part1), part2, String.class);
    }

    public static String[] combine(String[] part1, String part2) {
        return ObjectArrays.concat(part1, canonizePath(part2), String.class);
    }

    public static String[] combine(String[]... parts) {
        List<String> list = Stream.of(parts).flatMap(part -> Stream.of(part)).collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }

    public static String[] transformName(String[] parts, Function<String, String> transformer) {
        Preconditions.checkArgument(parts.length > 0);

        String[] copy = Arrays.copyOf(parts, parts.length);
        copy[copy.length - 1] = transformer.apply(copy[copy.length - 1]);
        return copy;
    }

    public static String[] transformNameModerately(String[] parts, Function<String, String> transformer) {
        if (parts.length > 0) {
            return transformName(parts, transformer);
        } else {
            return of(transformer.apply(""));
        }
    }

    public static boolean checkHead(String[] parts, String... head) {
        if (parts.length < head.length) {
            return false;
        }
        for (int i=0; i < head.length; ++i) {
            if (! Objects.equals(parts[i], head[i])) {
                return false;
            }
        }

        return true;
    }

    public static String[] tail(String[] parts) {
        return Arrays.copyOfRange(parts, 1, parts.length);
    }

    public static String[] drop(String[] parts, int n) {
        return Arrays.copyOfRange(parts, n, parts.length);
    }

    public static String[] dropHead(String[] parts, String... head) {
        Preconditions.checkArgument(checkHead(parts, head));
        return drop(parts, head.length);
    }

    public static String[] canonizePath(String path) {
        LinkedList<String> parts = Stream.of(path.split("/"))
            .filter(part -> part.length() > 0)
            .filter(part -> ! part.equals("."))
            .reduce(new LinkedList<String>(),
                    (list, part) -> {
                        if (part.equals("..")) {
                            list.removeLast();
                        } else {
                            list.add(part);
                        }
                        return list;
                    },
                    ($, $$) -> { $.addAll($$); return $; });

        return parts.toArray(new String[parts.size()]);
    }

    public static String stringify(String[] path) {
        return Joiner.on('/').join(path);
    }

    public static String[] of(String... parts) {
        return parts;
    }

}
