

import static spark.Spark.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.spi.FileTypeDetector;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.VerbatimSerializer;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class SparkTryout {

    private static File root = new File(".");
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

    public static void main(String[] args) {
        get("/", (req, resp)
                -> String.format("Hello world! I'm in %s", root.getAbsolutePath()));

        get("/raw/*", (req, resp) -> {
            String reqpath = req.splat()[0];
            File reqfile = new File(root, reqpath);

            String contents = IOUtils.toString(new FileReader(reqfile));

            resp.type("text/plain");
            return contents;
        });

        get("/view/*", (req, resp) -> {
            String reqpath = req.splat()[0];
            File reqfile = new File(root, reqpath);
            String ext = FilenameUtils.getExtension(reqpath);
            String contents = IOUtils.toString(new FileReader(reqfile));

            switch (ext) {
            case "md":
                String html = markdownProcessor.markdownToHtml(contents);
                resp.type("text/html");
                return html;
            default:
                resp.type("text/plain");
                return contents;
            }
        });

        get("/browser/*", (req, resp) -> {
            // TODO
            resp.status(500);
            return "not implemented yet";
        });

        get("/", (req, resp) -> {
           resp.redirect("/browser/");
           return "home at /browser/";
        });

        exception(FileNotFoundException.class, (throwable, req, resp) -> {
            String msg = String.format("File doesn't exists on server: %s",
                    throwable.getMessage());

            resp.status(404);
            resp.body(msg);
        });
    }

}
