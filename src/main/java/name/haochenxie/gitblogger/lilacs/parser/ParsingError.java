package name.haochenxie.gitblogger.lilacs.parser;

// TODO this should not be a runtime exception
public class ParsingError extends RuntimeException {

    private static final long serialVersionUID = 4366673631929639688L;

    public ParsingError() {
    }

    public ParsingError(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingError(String message) {
        super(message);
    }

    public ParsingError(Throwable cause) {
        super(cause);
    }

}
