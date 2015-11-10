package name.haochenxie.gitblogger;

import name.haochenxie.gitblogger.framework.renderer.ContentRenderer;

/**
 * @see {@link ContentRenderer}
 */
public class RendererException extends Exception {

    private static final long serialVersionUID = 2458200827095061788L;

    public RendererException(String message, Throwable cause) {
        super(message, cause);
    }

    public RendererException(String message) {
        super(message);
    }

    public RendererException(Throwable cause) {
        super(cause);
    }

}
