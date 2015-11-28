package name.haochenxie.gitblogger.config;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

public interface GitBloggerConfiguration {

    public BaseConfig getBaseConfig();

    public FSGitRepoConfig getRootRepoConfig();

    public Map<String, FSGitRepoConfig> getNonrootRepoConfigMap();

    public static GitBloggerConfiguration parseConfig(Properties prop) {
        BaseConfig baseConfig = BaseConfig.parseConfig(prop);
        FSGitRepoConfig rootRepoConfig = FSGitRepoConfig
                .parseRepoConfig(prop.getProperty("gitblogger.root", new File(".").getAbsolutePath()));

        String reposRaw = prop.getProperty("gitblogger.repos");
        Map<String, FSGitRepoConfig> repos = reposRaw == null
                ? Collections.emptyMap()
                : Splitter.on(',').withKeyValueSeparator('=')
                    .split(reposRaw).entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey,
                            e -> FSGitRepoConfig.parseRepoConfig(e.getValue())));

        return new GitBloggerConfiguration() {

            @Override
            public BaseConfig getBaseConfig() {
                return baseConfig;
            }

            @Override
            public FSGitRepoConfig getRootRepoConfig() {
                return rootRepoConfig;
            }

            @Override
            public Map<String, FSGitRepoConfig> getNonrootRepoConfigMap() {
                return repos;
            }

        };

    }

}
