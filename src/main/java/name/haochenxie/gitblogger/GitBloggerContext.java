package name.haochenxie.gitblogger;

import name.haochenxie.gitblogger.config.GitBloggerConfiguration;

public class GitBloggerContext {

    private GitBloggerConfiguration config;

    public GitBloggerContext(GitBloggerConfiguration config) {
        this.config = config;
    }

    public static GitBloggerContext createDefault() {
        return new GitBloggerContext(GitBloggerConfiguration.getCurrentConfig());
    }

    public GitBloggerConfiguration getConfig() {
        return config;
    }

}
