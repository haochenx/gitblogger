package name.haochenxie.gitblogger.framework.dispatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import spark.Request;
import spark.Response;

public class NamespacedPathDispatcherChain implements NamespacedPathDispatcher {

    private List<NamespacedPathDispatcher> dispatcherList;

    /**
     * @param parent if {@code null}, then set to self
     */
    public NamespacedPathDispatcherChain(ArrayList<NamespacedPathDispatcher> dispatcherList) {
        this.dispatcherList = Collections.unmodifiableList(dispatcherList);
    }

    public Collection<NamespacedPathDispatcher> getDispatcherList() {
        return dispatcherList;
    }

    @Override
    public Object dispatchPath(String path, Request req, Response resp, NamespacedDispatcherContext context)
            throws Exception {
        for (NamespacedPathDispatcher dispatcher : dispatcherList) {
            Object result = dispatcher.dispatchPath(path, req, resp, context);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

}
