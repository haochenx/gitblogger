package name.haochenxie.gitblogger;

public class DispatcherContext {

    private URIPathDispatcherChain uriPathDispatcherChain;

    public DispatcherContext(URIPathDispatcherChain uriPathDispatcherChain) {
        this.uriPathDispatcherChain = uriPathDispatcherChain;
    }

    public URIPathDispatcherChain getURIPathDispatcherChain() {
        return uriPathDispatcherChain;
    }

}
