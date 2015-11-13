package name.haochenxie.gitblogger.framework.dispatcher;

import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherBuilder.ChainNamespacedDispatcherBuilder;
import spark.Request;
import spark.Response;

public interface NamespacedDispatcher {

    public Object dispatch(String[] path, Request req, Response resp, NamespacedDispatcherContext context)
            throws Exception;

    public static ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<?>> createBuilder() {
        return new NamespacedDispatcherBuilder.ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<?>>();
    }

}
