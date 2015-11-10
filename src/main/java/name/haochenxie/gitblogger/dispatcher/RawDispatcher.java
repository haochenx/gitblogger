package name.haochenxie.gitblogger.dispatcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacePathDispatcher;
import spark.Request;
import spark.Response;

/**
 * the {@link NamespacePathDispatcher} that processes /raw/* URI's
 */
public class RawDispatcher implements NamespacePathDispatcher {

    @Override
    public Object dispatchNamespacePath(String reqpath, Request req, Response resp, DispatcherContext context)
            throws Exception {
        try {
            File root = context.getBloggerContext().getRoot();
            File reqfile = new File(root, reqpath);

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(reqfile));
            String mime = context.getMimeParser().parseMime(reqfile.getName());

            resp.type(mime);
            return input;
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

}
