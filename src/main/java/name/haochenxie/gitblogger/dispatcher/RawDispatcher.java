package name.haochenxie.gitblogger.dispatcher;

import java.io.BufferedInputStream;
import java.util.Arrays;

import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import spark.Request;
import spark.Response;

public class RawDispatcher implements ResourceDispatcher {

    @Override
    public Object dispatch(String[] rpath, Request req, Response resp, ResourceDispatcherContext context)
            throws Exception {
        System.out.println("raw: " + Arrays.toString(rpath));

        ResourceRepository repo = context.getResourceRepository();
        if (repo.checkExistence(rpath) && repo.checkIfResource(rpath)) {
            String basename = ResourceRepository.Helper.getBasename(rpath);
            String mime = context.getMimeParser().parseMime(basename);

            resp.type(mime);
            BufferedInputStream input = new BufferedInputStream(repo.open(rpath));
            return input;
        } else {
            return null;
        }
    }

}
