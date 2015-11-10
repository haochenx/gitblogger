package name.haochenxie.gitblogger.framework.dispatcher;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;

public class DispatcherContext {

    private URIPathDispatcherChain uriPathDispatcherChain;
    private MimeParser mimeParser;
    private ContentRendererRegisty contentRendererRegistry;
    private GitBloggerContext bloggerContext;

    public DispatcherContext(URIPathDispatcherChain uriPathDispatcherChain, MimeParser mimeParser,
            ContentRendererRegisty contentRendererRegistry, GitBloggerContext bloggerContext) {
        this.uriPathDispatcherChain = uriPathDispatcherChain;
        this.mimeParser = mimeParser;
        this.contentRendererRegistry = contentRendererRegistry;
        this.bloggerContext = bloggerContext;
    }

    public URIPathDispatcherChain getURIPathDispatcherChain() {
        return uriPathDispatcherChain;
    }

    public MimeParser getMimeParser() {
        return mimeParser;
    }

    public ContentRendererRegisty getContentRendererRegistry() {
        return contentRendererRegistry;
    }

    public GitBloggerContext getBloggerContext() {
        return bloggerContext;
    }

}
