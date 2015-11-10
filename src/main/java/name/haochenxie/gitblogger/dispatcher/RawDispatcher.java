package name.haochenxie.gitblogger.dispatcher;

import java.io.BufferedInputStream;

import name.haochenxie.gitblogger.framework.ResourcePathDispatcher;
import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import spark.Request;
import spark.Response;

public class RawDispatcher implements ResourcePathDispatcher {

    @Override
    public Object dispatchResourcePath(String rpath, Request req, Response resp,
            ResourceDispatcherContext context) throws Exception {
        ResourceRepository repo = context.getResourceRepository();
        String[] respath = repo.canonizePath(rpath);
        if (repo.checkExistence(respath) && repo.checkIfResource(respath)) {
            String basename = ResourceRepository.Helper.getBasename(respath);
            String mime = context.getMimeParser().parseMime(basename);

            resp.type(mime);
            BufferedInputStream input = new BufferedInputStream(repo.open(respath));
            return input;
        } else {
            return null;
        }
    }

}
