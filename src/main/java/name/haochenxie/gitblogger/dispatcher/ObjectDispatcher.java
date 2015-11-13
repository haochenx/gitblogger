package name.haochenxie.gitblogger.dispatcher;

import java.util.Optional;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherContext;
import spark.Request;
import spark.Response;
import spark.Spark;

public class ObjectDispatcher implements NamespacedDispatcher{

    private Repository gitrepo;

    public ObjectDispatcher(Repository gitrepo) {
        this.gitrepo = gitrepo;
    }

    @Override
    public Object dispatch(String[] path, Request req, Response resp, NamespacedDispatcherContext context)
            throws Exception {
        ObjectId objectId = ObjectId.fromString(path[0]);
        if (gitrepo.hasObject(objectId)) {
            Optional<String> asMime = Optional.ofNullable(req.queryParams("as"));
            resp.type(asMime.orElse("text/plain"));
            return gitrepo.getObjectDatabase().open(objectId).openStream();
        } else {
            Spark.halt(404, "sorry, i don't have the object you're requesting: " + objectId);
            return null;
        }
    }

}
