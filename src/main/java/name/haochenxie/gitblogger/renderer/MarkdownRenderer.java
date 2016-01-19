package name.haochenxie.gitblogger.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.VerbatimSerializer;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.RendererException;
import name.haochenxie.gitblogger.config.BaseConfig;
import name.haochenxie.gitblogger.framework.mime.MimeUtils;
import name.haochenxie.gitblogger.framework.renderer.ContentRenderer;

public class MarkdownRenderer implements ContentRenderer {

    protected Supplier<PegDownProcessor> pegdownProcessorSupplier = Suppliers.memoize(() -> new PegDownProcessor() {
        @Override
        public synchronized String markdownToHtml(char[] markdownSource,
                LinkRenderer linkRenderer,
                Map<String, VerbatimSerializer> verbatimSerializerMap,
                List<ToHtmlSerializerPlugin> plugins) {
            return super.markdownToHtml(markdownSource, linkRenderer, verbatimSerializerMap, plugins);
        }
    });

    private static final Set<String> SUPPORTED_SOURCE_MIME_TYPES = Collections.singleton("text/markdown");

    @Override
    public Set<String> getSupportedSourceMimeTypes() {
        return SUPPORTED_SOURCE_MIME_TYPES;
    }

    @Override
    public String render(String sourceMime, InputStream is, OutputStream os, GitBloggerContext context)
            throws RendererException, IOException {
        Preconditions.checkArgument(this.isSourceMimeTypeSupported(sourceMime),
                "source of MIME type %s is not supported", sourceMime);

        try {
            BaseConfig baseConfig = context.getConfig().getBaseConfig();
            Charset srcEncoding = baseConfig.getDefaultSourceEncoding();
            Charset outEncoding = baseConfig.getDefaultOutputEncoding();
            String source = IOUtils.toString(is, srcEncoding);

            try (Writer wr = new OutputStreamWriter(os, outEncoding)) {
                PegDownProcessor processor = pegdownProcessorSupplier.get();
                String html = processor.markdownToHtml(source);
                wr.write(html);
            }

            return MimeUtils.constructContentType("text/html", outEncoding);
        } catch (IOException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RendererException("failed to renderer Markdown file", ex);
        }
    }

}
