package name.haochenxie.gitblogger.framework.dispatcher;

import static name.haochenxie.gitblogger.framework.util.UriUtils.checkHead;
import static name.haochenxie.gitblogger.framework.util.UriUtils.combine;
import static name.haochenxie.gitblogger.framework.util.UriUtils.drop;
import static name.haochenxie.gitblogger.framework.util.UriUtils.tail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import name.haochenxie.gitblogger.framework.ResourceRepository;
import spark.Request;
import spark.Response;

public interface NamespacedDispatcherBuilder<B0 extends NamespacedDispatcherBuilder.BaseChainNamespacedDispatcherBuilder<?>> {

    public B0 finish();

    public <D extends NamespacedDispatcher> D build();

    public <D extends NamespacedDispatcher> D buildFinal();

    public interface BaseChainNamespacedDispatcherBuilder<B extends BaseChainNamespacedDispatcherBuilder<?>>
            extends NamespacedDispatcherBuilder<B> {

        @SuppressWarnings("rawtypes")
        public BaseChainNamespacedDispatcherBuilder subNamespace(String... namespace);

        @SuppressWarnings("rawtypes")
        public BaseChainNamespacedDispatcherBuilder dynamicSubNamespace(
                Function<String[], Optional<String[]>> namespaceSupplier);

        @SuppressWarnings("rawtypes")
        public BaseChainNamespacedDispatcherBuilder chain(NamespacedDispatcher dispatcher);

        @SuppressWarnings("rawtypes")
        public BaseChainNamespacedDispatcherBuilder dispatchLocation(String location, NamespacedDispatcher dispatcher);

        @SuppressWarnings("rawtypes")
        public ResourceChainNamespacedDispatcherBuilder withResourceRepository(
                Function<NamespacedDispatcherContext, ResourceRepository> repoSupplier);

        @SuppressWarnings("rawtypes")
        public ResourceChainNamespacedDispatcherBuilder withResourceRepository(ResourceRepository repo);

        @SuppressWarnings("rawtypes")
        public BaseChainNamespacedDispatcherBuilder forwardNamespace(String from, String to);

        @SuppressWarnings("rawtypes")
        public BaseChainNamespacedDispatcherBuilder chainForwarding(Function<String[], String[]> transformer);

    }

    @SuppressWarnings("unchecked")
    public class ResourceChainNamespacedDispatcherBuilder<B extends BaseChainNamespacedDispatcherBuilder<?>>
        extends ChainNamespacedDispatcherBuilder<B> {

        private Function<NamespacedDispatcherContext, ResourceRepository> repoSupplier;

        public ResourceChainNamespacedDispatcherBuilder(B parentBuilder,
                Function<NamespacedDispatcherContext, ResourceRepository> repoSupplier) {
            super(parentBuilder);
            this.repoSupplier = repoSupplier;
        }

        public ResourceChainNamespacedDispatcherBuilder<B>
        dispatchLocation(String location, ResourceDispatcher dispatcher) {
            return (ResourceChainNamespacedDispatcherBuilder<B>) super.dispatchLocation(location,
                    (path, req, resp, baseContext) -> {
                        ResourceDispatcherContext resContext = ResourceDispatcherContext.create(baseContext,
                                repoSupplier.apply(baseContext));
                        return dispatcher.dispatch(path, req, resp, resContext);
            });
        }

    }

    public class ChainNamespacedDispatcherBuilder<B extends BaseChainNamespacedDispatcherBuilder<?>>
        implements BaseChainNamespacedDispatcherBuilder<B> {

        private B parentBuilder;

        private ArrayList<NamespacedDispatcher> dispatcherChain = new ArrayList<>();

        public ChainNamespacedDispatcherBuilder() {
        }

        public ChainNamespacedDispatcherBuilder(B parentBuilder) {
            this.parentBuilder = parentBuilder;
        }

        public B getParentBuilder() {
            return parentBuilder;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>> subNamespace(String... namespace) {
            return new ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>>(this) {
                @Override
                public ChainNamespacedDispatcherBuilder<B> finish() {
                    NamespacedDispatcher child = build();
                    NamespacedDispatcher dispatcher = new NamespacedDispatcher() {

                        @Override
                        public Object dispatch(String[] path, Request req, Response resp, NamespacedDispatcherContext context) throws Exception {
                            if (checkHead(path, namespace)) {
                                NamespacedDispatcherContext subContext = NamespacedDispatcherContext.create(context,
                                        namespace, child);
                                return child.dispatch(tail(path), req, resp, subContext);
                            }
                            return null;
                        }
                    };
                    return this.getParentBuilder().chain(dispatcher);
                }
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>> dynamicSubNamespace(
                Function<String[], Optional<String[]>> namespaceSupplier) {
            return new ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>>(this) {
                @Override
                public ChainNamespacedDispatcherBuilder<B> finish() {
                    NamespacedDispatcher child = build();
                    NamespacedDispatcher dispatcher = new NamespacedDispatcher() {

                        @Override
                        public Object dispatch(String[] path, Request req, Response resp,
                                NamespacedDispatcherContext context) throws Exception {
                            Optional<String[]> namespace$ = namespaceSupplier.apply(path);

                            if (namespace$.isPresent()) {
                                String[] namespace = namespace$.get();
                                if (checkHead(path, namespace)) {
                                    NamespacedDispatcherContext subContext = NamespacedDispatcherContext.create(context,
                                            namespace, child);
                                    return child.dispatch(drop(path, namespace.length), req, resp, subContext);
                                }
                                return null;
                            } else {
                                return null;
                            }
                        }
                    };
                    return this.getParentBuilder().chain(dispatcher);
                }
            };
        }

        @Override
        public ChainNamespacedDispatcherBuilder<B>
        dispatchLocation(String location, NamespacedDispatcher dispatcher) {
            return this.chain((path, req, resp, context) -> {
                if (checkHead(path, location)) {
                    return dispatcher.dispatch(tail(path), req, resp, context);
                } else {
                    return null;
                }
            });
        }

        @Override
        public ChainNamespacedDispatcherBuilder<B> chain(NamespacedDispatcher dispatcher) {
            dispatcherChain.add(dispatcher);
            return this;
        }

        @SuppressWarnings("unchecked")
        public B finish() {
            Preconditions.checkNotNull(parentBuilder);
            return (B) parentBuilder.chain(build());
        }

        public static class ChainNamespacedDispacher implements NamespacedDispatcher {

            private List<NamespacedDispatcher> dispatcherChain;

            public ChainNamespacedDispacher(List<NamespacedDispatcher> dispatcherChain) {
                this.dispatcherChain = Collections.unmodifiableList(dispatcherChain);
            }

            @Override
            public Object dispatch(String[] path, Request req, Response resp, NamespacedDispatcherContext context)
                    throws Exception {
                for (NamespacedDispatcher dispatcher : dispatcherChain) {
                    Object result = dispatcher.dispatch(path, req, resp, context);
                    if (result != null) {
                        return result;
                    }
                }

                return null;
            }

        }

        @SuppressWarnings("unchecked")
        @Override
        public ChainNamespacedDispacher build() {
            return new ChainNamespacedDispacher(dispatcherChain);
        }

        @Override
        public ResourceChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>>
        withResourceRepository(
                Function<NamespacedDispatcherContext, ResourceRepository> repoSupplier) {
            return new ResourceChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>>(this, repoSupplier);
        }

        @Override
        public ResourceChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<B>>
        withResourceRepository(ResourceRepository repo) {
            return withResourceRepository($ -> repo);
        }

        @Override
        public ChainNamespacedDispatcherBuilder<B> forwardNamespace(
                String from, String to) {
            return (ChainNamespacedDispatcherBuilder<B>) this.subNamespace(from)
                .chain((path, req, resp, context) -> {
                    NamespacedDispatcherContext parentContext = context.getParentContext();
                    return parentContext.getCurrentNamespaceDispatcher().dispatch(combine(to, path), req, resp, parentContext);
                }).finish();
        }

        @Override
        public ChainNamespacedDispatcherBuilder<B> chainForwarding(
                Function<String[], String[]> transformer) {
            return chain((path, req, resp, context) -> {
               String[] tranformed = transformer.apply(path);
               return context.getCurrentNamespaceDispatcher().dispatch(tranformed, req, resp, context);
            });
        }

        @SuppressWarnings("unchecked")
        @Override
        public ChainNamespacedDispacher buildFinal() {
            Preconditions.checkState(parentBuilder == null);
            return build();
        }

    }

}
