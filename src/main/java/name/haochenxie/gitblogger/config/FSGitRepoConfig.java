package name.haochenxie.gitblogger.config;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface FSGitRepoConfig {

    public boolean isBare();

    public File getGitDir();

    public Optional<File> getWorkingDir();

    public String getProductionExposedRef();

    public default Optional<File> getIndexFile() {
        return isBare() ? Optional.empty() : Optional.of(new File(getGitDir(), "index"));
    }

    public static FSGitRepoConfig getCurrentRootRepoConfig() {
        String bareRootRepo = System.getProperty("gitblogger.bareRootRepo");
        String rootRepo = System.getProperty("gitblogger.rootRepo");
        String exposedRef = System.getProperty("gitblogger.rootExposedRef", "refs/heads/master");

        if (bareRootRepo != null) {
            return forBareGitDir(new File(bareRootRepo), exposedRef);
        } else if (rootRepo != null) {
            return forStandardGitDir(new File(rootRepo), exposedRef);
        } else {
            return forStandardGitDir(new File("."), exposedRef);
        }
    }

    // TODO maybe we should create a dedicated exception type for configuration errors
    // TODO probably catch the RepositoryNotFoundException
    public static FSGitRepoConfig parseRepoConfig(String spec) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("(?<path>[^@]+)(@(?<ref>.+))?");
        Matcher m = pattern.matcher(spec);
        if (m.matches()) {
            String path = m.group("path");
            String ref = m.group("ref");

            File root = new File(path);
            if (root.isDirectory()) {
                // a naive criteria for a bare git directory, but it
                // should be good enough for our use case
                boolean bare = ! new File(root, ".git").isDirectory();
                ref = ref != null ? ref : "refs/heads/master";

                if (bare) {
                    return forBareGitDir(root, ref);
                } else {
                    return forStandardGitDir(root, ref);
                }
            } else {
                throw new IllegalArgumentException("repository path " + root + "dosen't refer to a directory");
            }
        } else {
            throw new IllegalArgumentException("unable to parse path spec: " + spec);
        }
    }

    public static FSGitRepoConfig forStandardGitDir(File wdroot, String exposedRef) {
        return new FSGitRepoConfig() {

            @Override
            public boolean isBare() {
                return false;
            }

            @Override
            public Optional<File> getWorkingDir() {
                return Optional.of(wdroot);
            }

            @Override
            public File getGitDir() {
                return new File(wdroot, ".git");
            }

            @Override
            public String getProductionExposedRef() {
                return exposedRef;
            }

        };
    }

    public static FSGitRepoConfig forBareGitDir(File gitroot, String exposedRef) {
        return new FSGitRepoConfig() {

            @Override
            public boolean isBare() {
                return true;
            }

            @Override
            public Optional<File> getWorkingDir() {
                return Optional.empty();
            }

            @Override
            public File getGitDir() {
                return gitroot;
            }

            @Override
            public String getProductionExposedRef() {
                return exposedRef;
            }
        };
    }

}