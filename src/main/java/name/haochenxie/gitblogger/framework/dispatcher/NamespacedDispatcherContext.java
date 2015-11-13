package name.haochenxie.gitblogger.framework.dispatcher;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;

public interface NamespacedDispatcherContext extends DispatcherContext {

    public NamespacedDispatcher getCurrentNamespaceDispatcher();

    public NamespacedDispatcherContext getParentContext();

    public String[] getCurrentNamespace();

    public static NamespacedDispatcherContext create(DispatcherContext base, String[] namespace,
            NamespacedDispatcher currentNamespaceDispatcher) {
        return new NamespacedDispatcherContext() {

            @Override
            public NamespacedDispatcher getRootDispatcher() {
                return base.getRootDispatcher();
            }

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
            public NamespacedDispatcher getCurrentNamespaceDispatcher() {
                return currentNamespaceDispatcher;
            }

            @Override
            public NamespacedDispatcherContext getParentContext() {
                if (base instanceof NamespacedDispatcherContext) {
                    return (NamespacedDispatcherContext) base;
                } else {
                    return this;
                }
            }

            @Override
            public String[] getCurrentNamespace() {
                return namespace;
            }
        };
    }

}
