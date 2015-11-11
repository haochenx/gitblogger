package name.haochenxie.gitblogger.dispatcher;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import name.haochenxie.gitblogger.framework.ResourcePathDispatcher;
import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import spark.Request;
import spark.Response;

public class ViewDispatcher implements ResourcePathDispatcher {

    @Override
    public Object dispatchResourcePath(String rpath, Request req, Response resp,
            ResourceDispatcherContext context) throws Exception {
        System.out.print(String.format("view: %s (%s) .. ", rpath, req.pathInfo()));

        ResourceRepository repo = context.getResourceRepository();
        String[] respath = repo.canonizePath(rpath);

        if (repo.checkExistence(respath) && repo.checkIfResource(respath)) {
            String basename = ResourceRepository.Helper.getBasename(respath);
            String mime = context.getMimeParser().parseMime(basename);
            InputStream input = repo.open(respath);

            ContentRendererRegisty registry = context.getContentRendererRegistry();

            if (registry.isSourceMimeTypeSupported(mime)) {
                try (ByteArrayOutputStream buff = new ByteArrayOutputStream()) {
                    String outputMime = registry.render(mime, input, buff, context.getBloggerContext());
                    resp.type(outputMime);
                    System.out.println("handled");
                    return buff.toByteArray();
                }
            } else {
                System.out.println("relay to /raw");
                return context.getCurrentNamespaceDispatcher()
                        .dispatchPath("/raw/" + rpath, req, resp, context);
            }
        } else {
            System.out.println("not found");
            return null;
        }
    }

}
