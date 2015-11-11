package name.haochenxie.gitblogger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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

    public Repository openGitRepoOnRoot() throws IOException {
        Repository gitrepo = new FileRepositoryBuilder().setMustExist(true).setWorkTree(getRoot())
                .setGitDir(new File(getRoot(), ".git")).build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> { gitrepo.close(); }));
        return gitrepo;
    }

}
