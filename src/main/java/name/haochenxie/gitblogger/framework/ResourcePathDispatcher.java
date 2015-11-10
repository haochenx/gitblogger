package name.haochenxie.gitblogger.framework;

import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import spark.Request;
import spark.Response;

public interface ResourcePathDispatcher {

    /**
    *
    * @return if non-{@code null}, the request is regarded as dispatched and
    *         the returned value will be treated as result body; if
    *         {@code null}, the request is regarded as not dispatched and will
    *         be passed to the next dispatcher in the dispatcher chain.
    * @throws Exception
    *             if any checked exception is thrown, the request will be
    *             considered as dispatched and the framework will handle the
    *             exception but there will be no further normal dispatching
    */
    public Object dispatchResourcePath(String rpath, Request req, Response resp,
            ResourceDispatcherContext context) throws Exception;

}
