package name.haochenxie.gitblogger.dispatcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacePathDispatcher;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import spark.Request;
import spark.Response;

/**
 * the {@link NamespacePathDispatcher} that processes /view/* URI's
 */
public class ViewDispatcher implements NamespacePathDispatcher {

    @Override
    public Object dispatchNamespacePath(String reqpath, Request req, Response resp, DispatcherContext context)
            throws Exception {
        try {
            File root = context.getBloggerContext().getRoot();
            File reqfile = new File(root, reqpath);

            String mime = context.getMimeParser().parseMime(reqfile.getName());
            ContentRendererRegisty registry = context.getContentRendererRegistry();

            if (registry.isSourceMimeTypeSupported(mime)) {
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(reqfile));
                try (ByteOutputStream buff = new ByteOutputStream()) {
                    String outputMime = registry.render(mime, input, buff, context.getBloggerContext());
                    resp.type(outputMime);
                    return buff.getBytes();
                }
            } else {
                return context.getURIPathDispatcherChain().dispatchURIPath(
                        "/raw/" + reqpath, req, resp, context);
            }
        } catch (FileNotFoundException ex) {
            return null;
        }

    }

}
