package name.haochenxie.gitblogger;

import static spark.Spark.*;

public class GitBlogger {

    public static void main(String[] args) {

        URIPathDispatcherChain chain = new URIPathDispatcherChain.Builder()
                .addLocation("/raw", new RawDispatcher())
                .addLocation("/view", new ViewDispatcher())
                .addLocation("/browse", new BrowseDispatcher())
                .build();

        DispatcherContext context = new DispatcherContext(chain);

        get("/*", (req, resp) -> {
            String path = req.pathInfo();
            return chain.dispatchURIPath(path, req, resp, context);
        });
    }


}
