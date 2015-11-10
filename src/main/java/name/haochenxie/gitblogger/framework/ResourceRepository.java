package name.haochenxie.gitblogger.framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public interface ResourceRepository {

    public boolean checkExistence(String[] path) throws IOException;

    public InputStream open(String[] resourcePath) throws IOException;

    public boolean checkIfTree(String[] path) throws IOException;

    public boolean checkIfResource(String[] path) throws IOException;

    public interface TreeListing {

        public Collection<String> listChildrenTrees() throws IOException;

        public Collection<String> listChidrenResources() throws IOException;

    }

    public TreeListing createTreeListing(String[] treePath) throws IOException;

    /**
     * TODO doc, test
     *
     * @throws NoSuchElementException if {@code path} is like "../somewhere"
     */
    public default String[] canonizePath(String path) {
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

    public static class Helper {

        /**
         * @return the base name, or "/" if {@code respath} represents the root
         *         tree
         */
        public static String getBasename(String[] respath) {
            return respath.length == 0 ? "/" : respath[respath.length - 1];
        }

    }

}
