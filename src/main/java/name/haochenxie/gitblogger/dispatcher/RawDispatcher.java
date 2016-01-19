package name.haochenxie.gitblogger.dispatcher;

import java.io.BufferedInputStream;
import java.util.Arrays;

import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import name.haochenxie.gitblogger.framework.mime.MimeUtils;
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

            String contentType = mime;

            // when serving 'text/*' mime types, it is advisable to specify the encoding.
            // ref: RFC6657
            // ref: http://www.iana.org/assignments/media-types/media-types.xhtml#text
            if (mime.startsWith("text")) {
                contentType = MimeUtils.constructContentType(mime,
                        context.getBloggerContext().getConfig().getBaseConfig().getDefaultSourceEncoding());
            }

            resp.type(contentType);
            BufferedInputStream input = new BufferedInputStream(repo.open(rpath));
            return input;
        } else {
            return null;
        }
    }

}
