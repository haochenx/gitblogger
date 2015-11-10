package name.haochenxie.gitblogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Objects;

import spark.Request;
import spark.Response;

public class URIPathDispatcherChain implements URIPathDispatcher {

    private List<URIPathDispatcher> dispatcherList;

    public URIPathDispatcherChain(ArrayList<URIPathDispatcher> dispatcherList) {
        this.dispatcherList = Collections.unmodifiableList(dispatcherList);
    }

    public Collection<URIPathDispatcher> getDispatcherList() {
        return dispatcherList;
    }

    @Override
    public Object dispatchURIPath(String path, Request req, Response resp, DispatcherContext context) throws Exception {
        for (URIPathDispatcher dispatcher : dispatcherList) {
            Object result = dispatcher.dispatchURIPath(path, req, resp, context);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static class Builder {

        private ArrayList<URIPathDispatcher> dispatcherList = new ArrayList<>();

        public URIPathDispatcherChain build() {
            return new URIPathDispatcherChain(dispatcherList);
        }

        public Builder addDispatcher(URIPathDispatcher dispatcher) {
            dispatcherList.add(dispatcher);
            return this;
        }

        public Builder addForwardDispatcher(Function<String, String> uriPathTransformer) {
            URIPathDispatcher dispatcher = (path, req, resp, context) -> {
                URIPathDispatcherChain chain = context.getURIPathDispatcherChain();
                String transformedPath = uriPathTransformer.apply(path);

                if (Objects.equal(path, transformedPath)) {
                    throw new IllegalArgumentException("the transformer is not doing any transformation: "
                            + uriPathTransformer.getClass());
                }

                return chain.dispatchURIPath(transformedPath, req, resp, context);
            };

            return this.addDispatcher(dispatcher);
        }

        public Builder addLocation(String namespace, NamespacePathDispatcher dispatcher) {
            return this.addDispatcher(new LocationURIPathDispatcher(namespace, dispatcher));
        }

    }

}
