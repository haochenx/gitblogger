package name.haochenxie.gitblogger;

import java.io.File;

public class GitBloggerContext {

    public static final GitBloggerContext INSTANCE = new GitBloggerContext();

    private File root;

    protected GitBloggerContext(File root) {
        this.root = root;
    }

    private GitBloggerContext() {
        this(new File("."));
    }

    public File getRoot() {
        return root;
    }

}
