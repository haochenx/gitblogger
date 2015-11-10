package name.haochenxie.gitblogger.framework.dispatcher;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;

public interface DispatcherContext {

    public NamespacedPathDispatcher getRootDispatcher();

    public MimeParser getMimeParser();

    public ContentRendererRegisty getContentRendererRegistry();

    public GitBloggerContext getBloggerContext();

    public static DispatcherContext create(NamespacedPathDispatcher rootDispatcher, MimeParser mimeParser,
            ContentRendererRegisty contentRendererRegistry, GitBloggerContext bloggerContext) {
        return new DispatcherContext() {

            @Override
            public NamespacedPathDispatcher getRootDispatcher() {
                return rootDispatcher;
            }

            @Override
            public MimeParser getMimeParser() {
                return mimeParser;
            }

            @Override
            public ContentRendererRegisty getContentRendererRegistry() {
                return contentRendererRegistry;
            }

            @Override
            public GitBloggerContext getBloggerContext() {
                return bloggerContext;
            }

        };
    }


}
