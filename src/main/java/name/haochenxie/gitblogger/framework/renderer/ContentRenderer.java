package name.haochenxie.gitblogger.framework.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.RendererException;

public interface ContentRenderer {

    public Set<String> getSupportedSourceMimeTypes();

    public default boolean isSourceMimeTypeSupported(String mime) {
        return getSupportedSourceMimeTypes().contains(mime);
    }

    /**
     * @return the MIME type of the rendered content
     */
    public String render(String sourceMime, InputStream source, OutputStream output, GitBloggerContext context)
            throws RendererException, IOException;

}
