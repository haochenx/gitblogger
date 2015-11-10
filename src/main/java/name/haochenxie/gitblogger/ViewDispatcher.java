package name.haochenxie.gitblogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.VerbatimSerializer;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import spark.Request;
import spark.Response;

/**
 * the {@link NamespacePathDispatcher} that processes /view/* URI's
 */
public class ViewDispatcher implements NamespacePathDispatcher {

    private static PegDownProcessor markdownProcessor;

    static {
        markdownProcessor = new PegDownProcessor() {
            @Override
            public synchronized String markdownToHtml(char[] markdownSource, LinkRenderer linkRenderer,
                    Map<String, VerbatimSerializer> verbatimSerializerMap, List<ToHtmlSerializerPlugin> plugins) {
                return super.markdownToHtml(markdownSource, linkRenderer, verbatimSerializerMap, plugins);
            }

        };
    }

    @Override
    public Object dispatchNamespacePath(String reqpath, Request req, Response resp, DispatcherContext context)
            throws Exception {
        try {
            File root = GitBloggerContext.INSTANCE.getRoot();
            File reqfile = new File(root, reqpath);
            String ext = FilenameUtils.getExtension(reqpath);
            String contents = IOUtils.toString(new FileInputStream(reqfile));

            switch (ext) {
            case "html":
                resp.type("text/html");
                return contents;
            case "md":
                String html = markdownProcessor.markdownToHtml(contents);
                resp.type("text/html");
                return html;
            default:
                resp.type("text/plain");
                return contents;
            }
        } catch (FileNotFoundException ex) {
            return null;
        }

    }

}
