package name.haochenxie.gitblogger.config;

public interface GitBloggerConfiguration {

    public BaseConfig getBaseConfig();

    public FSGitRepoConfig getRootRepoConfig();

    public static GitBloggerConfiguration getCurrentConfig() {

        BaseConfig baseConfig = BaseConfig.getCurrentConfig();
        FSGitRepoConfig rootRepoConfig = FSGitRepoConfig.getCurrentRootRepoConfig();

        return new GitBloggerConfiguration() {

            @Override
            public BaseConfig getBaseConfig() {
                return baseConfig;
            }

            @Override
            public FSGitRepoConfig getRootRepoConfig() {
                return rootRepoConfig;
            }

        };

    }

}
