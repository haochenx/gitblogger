package name.haochenxie.gitblogger;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;

import spark.Request;
import spark.Response;

/**
 * the {@link NamespacePathDispatcher} that processes /raw/* URI's
 */
public class RawDispatcher implements NamespacePathDispatcher {

    @Override
    public Object dispatchNamespacePath(String reqpath, Request req, Response resp, DispatcherContext context)
            throws Exception {
        File root = GitBloggerContext.INSTANCE.getRoot();
        File reqfile = new File(root, reqpath);

        String contents = IOUtils.toString(new FileReader(reqfile));

        resp.type("text/plain");
        return contents;
    }

}
