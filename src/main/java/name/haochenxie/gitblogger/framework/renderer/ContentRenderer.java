package name.haochenxie.gitblogger.framework.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.RendererException;
import name.haochenxie.gitblogger.framework.mime.MimeUtils;

public interface ContentRenderer {

    public Set<String> getSupportedSourceMimeTypes();

    public default boolean isSourceMimeTypeSupported(String mime) {
        return getSupportedSourceMimeTypes().contains(mime);
    }

    /**
     * @return the Content-Type of the rendered content. it is usually a MIME
     *         type string, but could also contain other parameters, such as
     *         "charset=*". should an charset parameter be included, it is
     *         advisable to use
     *         {@link MimeUtils#constructContentType(String, java.nio.charset.Charset)}
     */
    public String render(String sourceMime, InputStream source, OutputStream output, GitBloggerContext context)
            throws RendererException, IOException;

}
