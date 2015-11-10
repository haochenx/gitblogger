package name.haochenxie.gitblogger.framework.dispatcher;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

import name.haochenxie.gitblogger.framework.ResourcePathDispatcher;
import name.haochenxie.gitblogger.framework.ResourceRepository;

public class NamespacedDispatcherChainBuilder {

    private ArrayList<NamespacedPathDispatcher> dispatcherList = new ArrayList<>();

    public NamespacedPathDispatcherChain build() {
        return new NamespacedPathDispatcherChain(dispatcherList);
    }

    public NamespacedDispatcherChainBuilder addDispatcher(NamespacedPathDispatcher dispatcher) {
        dispatcherList.add(dispatcher);
        return this;
    }

    public NamespacedDispatcherChainBuilder addForwardDispatcher(Function<String, String> uriPathTransformer) {
        NamespacedPathDispatcher dispatcher = (path, req, resp, context) -> {
            NamespacedPathDispatcher redispatcher = context.getCurrentNamespaceDispatcher();
            String transformedPath = uriPathTransformer.apply(path);

            if (Objects.equals(path, transformedPath)) {
                throw new IllegalArgumentException(
                        "the transformer is not doing any transformation: " + uriPathTransformer.getClass());
            }

            return redispatcher.dispatchPath(transformedPath, req, resp, context);
        };

        return this.addDispatcher(dispatcher);
    }

    public NamespacedDispatcherChainBuilder addLocation(String namespace, NamespacedPathDispatcher dispatcher) {
        return this.addDispatcher(new LocationPathDispatcher(namespace, dispatcher));
    }

    public class ResourceRepositoryEnhancedNamespacedDispatcherChaniBuilder extends NamespacedDispatcherChainBuilder {

        private ResourceRepository repo;

        public ResourceRepositoryEnhancedNamespacedDispatcherChaniBuilder(ResourceRepository repo) {
            this.repo = repo;
        }

        public ResourceRepositoryEnhancedNamespacedDispatcherChaniBuilder addLocation(String namespace,
                ResourcePathDispatcher dispatcher) {
            NamespacedPathDispatcher innerDispatcher = (path, req, resp, namedContext) -> {
                ResourceDispatcherContext rcontext = ResourceDispatcherContext.create(namedContext, repo);
                return dispatcher.dispatchResourcePath(path, req, resp, rcontext);
            };

            this.addDispatcher(new LocationPathDispatcher(namespace, innerDispatcher));
            return this;
        }

        public NamespacedDispatcherChainBuilder withoutRepository() {
            return NamespacedDispatcherChainBuilder.this;
        }

    }

    public ResourceRepositoryEnhancedNamespacedDispatcherChaniBuilder withRepository(ResourceRepository repo) {
        return new ResourceRepositoryEnhancedNamespacedDispatcherChaniBuilder(repo);
    }

}