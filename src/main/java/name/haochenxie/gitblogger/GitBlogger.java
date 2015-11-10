package name.haochenxie.gitblogger;

import static spark.Spark.*;

import name.haochenxie.gitblogger.dispatcher.BrowseDispatcher;
import name.haochenxie.gitblogger.dispatcher.RawDispatcher;
import name.haochenxie.gitblogger.dispatcher.ViewDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherChainBuilder;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedPathDispatcherChain;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.framework.repo.FileSystemResourceRepository;
import name.haochenxie.gitblogger.mime.SimpleMimeParser;
import name.haochenxie.gitblogger.renderer.MarkdownRenderer;

public class GitBlogger {

    public static void main(String[] args) {

        GitBloggerContext bloggerContext = GitBloggerContext.createDefault();

        FileSystemResourceRepository repo = new FileSystemResourceRepository(bloggerContext.getRoot());

        NamespacedPathDispatcherChain chain = new NamespacedDispatcherChainBuilder()
                .withRepository(repo)
                .addLocation("/raw", new RawDispatcher())
                .addLocation("/view", new ViewDispatcher())
                .addLocation("/browse", new BrowseDispatcher())
                .build();

        MimeParser mimeParser = new SimpleMimeParser();

        ContentRendererRegisty rendererRegistry = new ContentRendererRegisty()
                .register(new MarkdownRenderer());

        DispatcherContext dispatcherContext = DispatcherContext.create(chain, mimeParser, rendererRegistry,
                bloggerContext);

        NamespacedDispatcherContext rootDispatcherContext =
                NamespacedDispatcherContext.create(dispatcherContext, chain);

        get("/*", (req, resp) -> {
            String path = req.pathInfo();
            Object result = chain.dispatchPath(path, req, resp, rootDispatcherContext);

            if (result == null) {
                halt(404, "(HTTP Status: 404) Well, not found!");
            }

            return result;
        });
    }


}
