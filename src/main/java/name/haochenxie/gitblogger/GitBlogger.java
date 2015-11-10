package name.haochenxie.gitblogger;

import static spark.Spark.*;

import name.haochenxie.gitblogger.dispatcher.BrowseDispatcher;
import name.haochenxie.gitblogger.dispatcher.RawDispatcher;
import name.haochenxie.gitblogger.dispatcher.ViewDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.URIPathDispatcherChain;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.mime.SimpleMimeParser;
import name.haochenxie.gitblogger.renderer.MarkdownRenderer;

public class GitBlogger {

    public static void main(String[] args) {

        URIPathDispatcherChain chain = new URIPathDispatcherChain.Builder()
                .addLocation("/raw", new RawDispatcher())
                .addLocation("/view", new ViewDispatcher())
                .addLocation("/browse", new BrowseDispatcher())
                .build();

        MimeParser mimeParser = new SimpleMimeParser();

        ContentRendererRegisty rendererRegistry = new ContentRendererRegisty()
                .register(new MarkdownRenderer());

        GitBloggerContext bloggerContext = GitBloggerContext.createDefault();
        DispatcherContext dispatcherContext = new DispatcherContext(chain, mimeParser, rendererRegistry,
                bloggerContext);

        get("/*", (req, resp) -> {
            String path = req.pathInfo();
            return chain.dispatchURIPath(path, req, resp, dispatcherContext);
        });
    }


}
