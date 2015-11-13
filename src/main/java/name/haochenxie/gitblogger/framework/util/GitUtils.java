package name.haochenxie.gitblogger.framework.util;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import name.haochenxie.gitblogger.config.FSGitRepoConfig;

public class GitUtils {

    public static Repository openGitRepository(FSGitRepoConfig repoConfig) throws IOException {
      FileRepositoryBuilder builder = new FileRepositoryBuilder()
          .setMustExist(true)
          .setGitDir(repoConfig.getGitDir());

      if (! repoConfig.isBare()) {
          repoConfig.getWorkingDir().map(wd -> repoConfig.getIndexFile().map(idx -> {
              builder
                  .setWorkTree(wd)
                  .setIndexFile(idx);
              return null;
          }));
      }

      Repository gitrepo = builder.build();

      Runtime.getRuntime().addShutdownHook(new Thread(() -> { gitrepo.close(); }));
      return gitrepo;
    }

}
