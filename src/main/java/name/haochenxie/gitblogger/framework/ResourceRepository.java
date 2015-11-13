package name.haochenxie.gitblogger.framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

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
