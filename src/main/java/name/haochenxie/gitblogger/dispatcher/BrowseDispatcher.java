package name.haochenxie.gitblogger.dispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacePathDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.URIPathDispatcherChain;
import spark.Request;
import spark.Response;

public class BrowseDispatcher implements NamespacePathDispatcher {

    Map<Request, AtomicInteger> magicCounterMap = new ConcurrentHashMap<>();

    @Override
    public Object dispatchNamespacePath(String reqpath, Request req, Response resp, DispatcherContext context)
            throws Exception {
        magicCounterMap.putIfAbsent(req, new AtomicInteger(0));

        URIPathDispatcherChain chain = new URIPathDispatcherChain.Builder()
                .addForwardDispatcher(path -> String.format("/view/%s", reqpath))
                .addForwardDispatcher(path -> String.format("/view/%s.html", reqpath))
                .addForwardDispatcher(path -> String.format("/view/%s.md", reqpath))
                .addForwardDispatcher(path -> {
                    if (magicCounterMap.get(req).getAndIncrement() > 0) {
                        // we don't want a infinite loop
                        return String.format("/view/%s", reqpath);
                    } else {
                        return String.format("/browse/%s/index", reqpath);
                    }
                })
                .build();

        return chain.dispatchURIPath(reqpath, req, resp, context);
    }

}
