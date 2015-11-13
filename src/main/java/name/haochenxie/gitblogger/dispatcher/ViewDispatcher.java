package name.haochenxie.gitblogger.dispatcher;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.framework.util.UriUtils;
import spark.Request;
import spark.Response;

public class ViewDispatcher implements ResourceDispatcher {

    @Override
    public Object dispatch(String[] rpath, Request req, Response resp, ResourceDispatcherContext context)
            throws Exception {
        System.out.print(String.format("view: %s", Arrays.toString(rpath)));

        ResourceRepository repo = context.getResourceRepository();

        if (repo.checkExistence(rpath) && repo.checkIfResource(rpath)) {
            String basename = ResourceRepository.Helper.getBasename(rpath);
            String mime = context.getMimeParser().parseMime(basename);
            InputStream input = repo.open(rpath);

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
                        .dispatch(UriUtils.combine("raw", rpath), req, resp, context);
            }
        } else {
            System.out.println("not found");
            return null;
        }
    }

}
