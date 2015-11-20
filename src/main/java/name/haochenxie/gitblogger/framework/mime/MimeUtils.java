package name.haochenxie.gitblogger.framework.mime;

import java.nio.charset.Charset;

public class MimeUtils {

    public static String constructContentType(String mime, Charset charset) {
        return String.format("%s;charset=%s", mime, charset.name());
    }

}
