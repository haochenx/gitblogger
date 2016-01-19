package name.haochenxie.gitblogger.config;

import java.io.File;
import java.util.Optional;

public interface FSGitRepoConfig {

    public static String CONFKEY_ROOT_REPO_BARE = "gitblogger.bareRootRepo";
    public static String CONFKEY_ROOT_REPO = "gitblogger.rootRepo";
    public static String CONFKEY_EXPOSED_REF = "gitblogger.rootExposedRef";

    public static String DEFAULT_EXPOSED_REF = "refs/heads/master";

    public boolean isBare();

    public File getGitDir();

    public Optional<File> getWorkingDir();

    public String getProductionExposedRef();

    public default Optional<File> getIndexFile() {
        return isBare() ? Optional.empty() : Optional.of(new File(getGitDir(), "index"));
    }

    public static FSGitRepoConfig getCurrentRootRepoConfig() {
        String bareRootRepo = System.getProperty(CONFKEY_ROOT_REPO_BARE);
        String rootRepo = System.getProperty(CONFKEY_ROOT_REPO);
        String exposedRef = System.getProperty(CONFKEY_EXPOSED_REF, DEFAULT_EXPOSED_REF);

        if (bareRootRepo != null) {
            return forBareGitDir(new File(bareRootRepo), exposedRef);
        } else if (rootRepo != null) {
            return forStandardGitDir(new File(rootRepo), exposedRef);
        } else {
            return forStandardGitDir(new File("."), exposedRef);
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