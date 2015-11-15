package name.haochenxie.gitblogger.dispatcher;

import static name.haochenxie.gitblogger.framework.util.UriUtils.combine;
import static name.haochenxie.gitblogger.framework.util.UriUtils.transformNameModerately;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import spark.Request;
import spark.Response;

public class BrowseDispatcher implements ResourceDispatcher {

    Map<Request, AtomicInteger> magicCounterMap = new ConcurrentHashMap<>();

    @Override
    public Object dispatch(String[] rpath, Request req, Response resp,
            ResourceDispatcherContext context) throws Exception {
        magicCounterMap.putIfAbsent(req, new AtomicInteger(0));

        NamespacedDispatcher chain = NamespacedDispatcher.createBuilder()
                .chainForwarding(path -> combine("view", path))
                .chainForwarding(path -> combine("view", transformNameModerately(path, name -> name + ".html")))
                .chainForwarding(path -> combine("view", transformNameModerately(path, name -> name + ".md")))
                .chainForwarding(path -> {
                    if (magicCounterMap.get(req).getAndIncrement() > 0) {
                        // we don't want a infinite loop
                        return combine("view", path);
                    } else {
                        return combine(combine("browse", path), "index");
                    }
                })
                .build();

        return chain.dispatch(rpath, req, resp, context);
    }

}
