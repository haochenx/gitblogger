package name.haochenxie.gitblogger.framework.dispatcher;

import spark.Request;
import spark.Response;

public interface ResourceDispatcher {

    public Object dispatch(String[] path, Request req, Response resp, ResourceDispatcherContext context) throws Exception;

}
