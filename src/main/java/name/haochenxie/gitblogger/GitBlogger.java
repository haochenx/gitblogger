package name.haochenxie.gitblogger;

import static name.haochenxie.gitblogger.framework.util.UriUtils.canonizePath;
import static name.haochenxie.gitblogger.framework.util.UriUtils.of;
import static name.haochenxie.gitblogger.framework.util.UriUtils.stringify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Joiner;

import name.haochenxie.gitblogger.dispatcher.BrowseDispatcher;
import name.haochenxie.gitblogger.dispatcher.ObjectDispatcher;
import name.haochenxie.gitblogger.dispatcher.RawDispatcher;
import name.haochenxie.gitblogger.dispatcher.ViewDispatcher;
import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcher;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.framework.repo.FileSystemResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitIndexResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitTreeResourceRepository;
import name.haochenxie.gitblogger.mime.SimpleMimeParser;
import name.haochenxie.gitblogger.renderer.MarkdownRenderer;
import spark.Spark;

public class GitBlogger {

    public static void main(String[] args) throws Exception {
        GitBloggerContext bloggerContext = GitBloggerContext.createDefault();
        NamespacedDispatcher rootDispatcher = createRootDispatcher(bloggerContext);
        ContentRendererRegisty rendererRegistry = new ContentRendererRegisty()
                .register(new MarkdownRenderer());
        SimpleMimeParser mimeParser = new SimpleMimeParser();

        DispatcherContext baseContext =
                DispatcherContext.create(rootDispatcher, mimeParser, rendererRegistry, bloggerContext);
        NamespacedDispatcherContext dispatcherContext = NamespacedDispatcherContext.create(baseContext, of("/"),
                rootDispatcher);

        Spark.port(4568);

        Spark.get("/*", (req, resp) -> {
            String spath = req.pathInfo();
            String[] path = canonizePath(spath);

            return rootDispatcher.dispatch(path, req, resp, dispatcherContext);
        });
    }

    private static NamespacedDispatcher createRootDispatcher(GitBloggerContext bloggerContext) throws IOException {
        ResourceDispatcher rawDispatcher = new RawDispatcher();
        ResourceDispatcher viewDispatcher = new ViewDispatcher();
        ResourceDispatcher browseDispatcher = new BrowseDispatcher();

        ResourceRepository wtrepo = new FileSystemResourceRepository(bloggerContext.getRoot());
        Repository gitrepo = bloggerContext.openGitRepoOnRoot();
        GitIndexResourceRepository idxrepo = new GitIndexResourceRepository(DirCache.read(gitrepo),
                gitrepo.newObjectReader());

        NamespacedDispatcher rootDispatcher = NamespacedDispatcher.createBuilder()
            .subNamespace("worktree")
                .withResourceRepository(wtrepo)
                    .dispatchLocation("raw", rawDispatcher)
                    .dispatchLocation("view", viewDispatcher)
                    .dispatchLocation("browse", browseDispatcher)
                    .finish()
                .finish()
            .subNamespace("index")
                .withResourceRepository(idxrepo)
                    .dispatchLocation("raw", rawDispatcher)
                    .dispatchLocation("view", viewDispatcher)
                    .dispatchLocation("browse", browseDispatcher)
                    .finish()
                .finish()
            .subNamespace("refs")
                .dynamicSubNamespace(path -> parseRef(path, gitrepo))
                    .withResourceRepository(context -> createRefResourceRepository(context, gitrepo))
                        .dispatchLocation("raw", rawDispatcher)
                        .dispatchLocation("view", viewDispatcher)
                        .dispatchLocation("browse", browseDispatcher)
                        .finish()
                    .finish()
                .finish()
            .subNamespace("commit")
                .dynamicSubNamespace(path -> parseCommitOid(path, gitrepo))
                    .withResourceRepository(context -> createCommitResourceRepository(context, gitrepo))
                        .dispatchLocation("raw", rawDispatcher)
                        .dispatchLocation("view", viewDispatcher)
                        .dispatchLocation("browse", browseDispatcher)
                        .finish()
                    .finish()
                .finish()
            .subNamespace("tree")
                .dynamicSubNamespace(path -> parseTreeOid(path, gitrepo))
                    .withResourceRepository(context -> createTreeResourceRepository(context, gitrepo))
                        .dispatchLocation("raw", rawDispatcher)
                        .dispatchLocation("view", viewDispatcher)
                        .dispatchLocation("browse", browseDispatcher)
                        .finish()
                    .finish()
                .finish()
            .subNamespace("object")
                .chain(new ObjectDispatcher(gitrepo))
                .finish()
            .forwardNamespace("expose", "index")
            .buildFinal();

        return rootDispatcher;
    }

    private static ResourceRepository createRefResourceRepository(NamespacedDispatcherContext context,
            Repository gitrepo) {
        try {
            String[] namespace = context.getCurrentNamespace();
            String ref = stringify(namespace);
            Set<String> reflist = gitrepo.getAllRefs().keySet();

            if (! reflist.contains(ref)) {
                ref = "refs/" + ref;
            }

            GitTreeResourceRepository repo;
            repo = GitTreeResourceRepository.forRef(gitrepo, ref);
            return repo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResourceRepository createCommitResourceRepository(NamespacedDispatcherContext context,
            Repository gitrepo) {
        try {
            String[] namespace = context.getCurrentNamespace();
            String commitId = stringify(namespace);

            ResourceRepository repo = GitTreeResourceRepository.forCommit(gitrepo, commitId);
            return repo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResourceRepository createTreeResourceRepository(NamespacedDispatcherContext context,
            Repository gitrepo) {
        try {
            String[] namespace = context.getCurrentNamespace();
            String treeId = stringify(namespace);

            ResourceRepository repo = GitTreeResourceRepository.forTree(gitrepo, treeId);
            return repo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<String[]> parseCommitOid(String[] path, Repository gitrepo) {
        if (path.length > 0) {
            return Optional.of(of(path[0]));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String[]> parseTreeOid(String[] path, Repository gitrepo) {
        if (path.length > 0) {
            return Optional.of(of(path[0]));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String[]> parseRef(String[] path, Repository gitrepo) {
      Set<String> reflist = gitrepo.getAllRefs().keySet();

      for (int i = 0; i < path.length; ++i) {
          String comp = Joiner.on('/').join(Arrays.asList(path).subList(0, i));
          List<String> candidates = Arrays.asList(comp, "refs/" + comp);
          for (String candidate : candidates) {
              if (reflist.contains(candidate)) {
                  return Optional.of(canonizePath(comp));
              }
          }
      }

      return Optional.empty();
    }

}
