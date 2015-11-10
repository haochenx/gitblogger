package name.haochenxie.gitblogger.framework.dispatcher;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;

public interface NamespacedDispatcherContext extends DispatcherContext {

    public NamespacedPathDispatcher getCurrentNamespaceDispatcher();

    public static NamespacedDispatcherContext create(DispatcherContext base,
            NamespacedPathDispatcher currentNamespaceDispatcher) {
        return new NamespacedDispatcherContext() {

            @Override
            public NamespacedPathDispatcher getRootDispatcher() {
                return base.getRootDispatcher();
            }

            @Override
            public MimeParser getMimeParser() {
                return base.getMimeParser();
            }

            @Override
            public ContentRendererRegisty getContentRendererRegistry() {
                return base.getContentRendererRegistry();
            }

            @Override
            public GitBloggerContext getBloggerContext() {
                return base.getBloggerContext();
            }

            @Override
            public NamespacedPathDispatcher getCurrentNamespaceDispatcher() {
                return currentNamespaceDispatcher;
            }
        };
    }

}
