package name.haochenxie.gitblogger.framework.dispatcher;

import spark.Request;
import spark.Response;

public class LocationPathDispatcher implements NamespacedPathDispatcher {

    /**
     * location should be like "/loc", with exactly one leading slash and no trailing slash
     */
    private String location;

    private NamespacedPathDispatcher innerDispatcher;

    private NamespacedPathDispatcher newNamespaceDispatcher;

    public LocationPathDispatcher(String location, NamespacedPathDispatcher innerDispatcher) {
        this.location = Helper.decorate(location);
        this.innerDispatcher = innerDispatcher;
    }

    @Override
    public Object dispatchPath(String path, Request req, Response resp, NamespacedDispatcherContext context)
            throws Exception {
        path = Helper.decorate(path);

        if (newNamespaceDispatcher != null) {
            context = NamespacedDispatcherContext.create(context, newNamespaceDispatcher);
        }
        if (path.startsWith(location)) {
            String restpath = path.substring(location.length());
            return innerDispatcher.dispatchPath(restpath, req, resp, context);
        } else {
            return null;
        }
    }

    public void setNewNamespaceDispatcher(NamespacedPathDispatcher d) {
        this.newNamespaceDispatcher = d;
    }

    public static class Helper {

        public static String decorate(String location) {
            if (! location.startsWith("/")) {
                return decorate("/" + location);
            } else if (location.endsWith("/")) {
                return decorate(location.substring(0, location.length()-1));
            } else if (location.startsWith("//")) {
                return decorate(location.substring(1));
            } else {
                return location;
            }
        }

    }

}