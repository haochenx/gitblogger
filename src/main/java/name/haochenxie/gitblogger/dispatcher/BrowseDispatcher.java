package name.haochenxie.gitblogger.dispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import name.haochenxie.gitblogger.framework.ResourcePathDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherChainBuilder;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedPathDispatcherChain;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcherContext;
import spark.Request;
import spark.Response;

public class BrowseDispatcher implements ResourcePathDispatcher {

    Map<Request, AtomicInteger> magicCounterMap = new ConcurrentHashMap<>();

    @Override
    public Object dispatchResourcePath(String rpath, Request req, Response resp,
            ResourceDispatcherContext context) throws Exception {
        magicCounterMap.putIfAbsent(req, new AtomicInteger(0));

        NamespacedPathDispatcherChain chain = new NamespacedDispatcherChainBuilder()
                .addForwardDispatcher(path -> String.format("/view/%s", rpath))
                .addForwardDispatcher(path -> String.format("/view/%s.html", rpath))
                .addForwardDispatcher(path -> String.format("/view/%s.md", rpath))
                .addForwardDispatcher(path -> {
                    if (magicCounterMap.get(req).getAndIncrement() > 0) {
                        // we don't want a infinite loop
                        return String.format("/view/%s", rpath);
                    } else {
                        return String.format("/browse/%s/index", rpath);
                    }
                })
                .build();

        return chain.dispatchPath(rpath, req, resp, context);
    }

}
