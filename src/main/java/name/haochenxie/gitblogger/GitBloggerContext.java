package name.haochenxie.gitblogger;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GitBloggerContext {

    private File root;

    private Charset defaultSourceEncoding;

    private Charset defaultOutputEncoding;

    public GitBloggerContext(File root, Charset defaultSourceEncoding, Charset defaultOutputEncoding) {
        this.root = root;
        this.defaultSourceEncoding = defaultSourceEncoding;
        this.defaultOutputEncoding = defaultOutputEncoding;
    }

    public static GitBloggerContext createDefault() {
        return new GitBloggerContext(new File("."), StandardCharsets.UTF_8, StandardCharsets.UTF_8);
    }

    public File getRoot() {
        return root;
    }

    public Charset getDefaultSourceEncoding() {
        return defaultSourceEncoding;
    }

    public Charset getDefaultOutputEncoding() {
        return defaultOutputEncoding;
    }

}
