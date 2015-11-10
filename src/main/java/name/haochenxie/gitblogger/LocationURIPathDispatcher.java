package name.haochenxie.gitblogger;

import spark.Request;
import spark.Response;

public class LocationURIPathDispatcher implements URIPathDispatcher {

    private String location;
    private NamespacePathDispatcher childDispatcher;

    public LocationURIPathDispatcher(String location, NamespacePathDispatcher childDispatcher) {
        this.location = Helper.decorate(location);
        this.childDispatcher = childDispatcher;
    }

    @Override
    public Object dispatchURIPath(String path, Request req, Response resp, DispatcherContext context)
            throws Exception {
        if (path.startsWith(location)) {
            String restpath = path.substring(location.length());
            return childDispatcher.dispatchNamespacePath(restpath, req, resp, context);
        } else {
            return null;
        }
    }

    public static class Helper {

        public static String decorate(String location) {
            if (! location.startsWith("/")) {
                return decorate("/" + location);
            } else if (! location.endsWith("/")) {
                return decorate(location + "/");
            } else {
                return location;
            }
        }

    }

}