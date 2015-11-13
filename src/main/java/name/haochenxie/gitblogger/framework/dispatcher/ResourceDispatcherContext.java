package name.haochenxie.gitblogger.framework.dispatcher;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;

public interface ResourceDispatcherContext extends NamespacedDispatcherContext {

    public ResourceRepository getResourceRepository();

    public static ResourceDispatcherContext create(NamespacedDispatcherContext base, ResourceRepository repo) {
        return new ResourceDispatcherContext() {

            @Override
            public ResourceRepository getResourceRepository() {
                return repo;
            }

            @Override
            public NamespacedDispatcher getRootDispatcher() {
                return base.getRootDispatcher();
            }

            @Override
            public NamespacedDispatcher getCurrentNamespaceDispatcher() {
                return base.getCurrentNamespaceDispatcher();
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
            public NamespacedDispatcherContext getParentContext() {
                return base.getParentContext();
            }

            @Override
            public String[] getCurrentNamespace() {
                return base.getCurrentNamespace();
            }

        };
    }

}
