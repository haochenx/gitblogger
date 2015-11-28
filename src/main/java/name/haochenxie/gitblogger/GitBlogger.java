package name.haochenxie.gitblogger;

import static name.haochenxie.gitblogger.framework.util.UriUtils.canonizePath;
import static name.haochenxie.gitblogger.framework.util.UriUtils.checkHead;
import static name.haochenxie.gitblogger.framework.util.UriUtils.combine;
import static name.haochenxie.gitblogger.framework.util.UriUtils.drop;
import static name.haochenxie.gitblogger.framework.util.UriUtils.dropHead;
import static name.haochenxie.gitblogger.framework.util.UriUtils.of;
import static name.haochenxie.gitblogger.framework.util.UriUtils.stringify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Joiner;

import name.haochenxie.gitblogger.config.BaseConfig;
import name.haochenxie.gitblogger.config.FSGitRepoConfig;
import name.haochenxie.gitblogger.config.GitBloggerConfiguration;
import name.haochenxie.gitblogger.dispatcher.BrowseDispatcher;
import name.haochenxie.gitblogger.dispatcher.ObjectDispatcher;
import name.haochenxie.gitblogger.dispatcher.RawDispatcher;
import name.haochenxie.gitblogger.dispatcher.ViewDispatcher;
import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcher;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherBuilder.ChainNamespacedDispatcherBuilder;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherBuilder.ChainNamespacedDispatcherBuilder.ChainNamespacedDispacher;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.ResourceDispatcher;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.framework.repo.FileSystemResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitIndexResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitRefResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitTreeResourceRepository;
import name.haochenxie.gitblogger.framework.util.GitUtils;
import name.haochenxie.gitblogger.framework.util.SuperOptional;
import name.haochenxie.gitblogger.mime.SimpleMimeParser;
import name.haochenxie.gitblogger.renderer.MarkdownRenderer;
import spark.Spark;

public class GitBlogger {

    public static void main(String[] args) throws Exception {
        GitBloggerContext bloggerContext = GitBloggerContext.createDefault();

        logConfig(bloggerContext.getConfig());

        BaseConfig bconfig = bloggerContext.getConfig().getBaseConfig();

        String rootUrl = SuperOptional.from(bconfig.getCanonicalUrl())
                .failableMap(url -> new URI(url).getPath()).orElse("");
        String[] rootNamespace = canonizePath(rootUrl);
        String sparkListenerPath = "/" + stringify(combine(rootNamespace, "*"));

        NamespacedDispatcher rootDispatcher = bconfig.isProductionMode()
                ? createProductionRootDispatcher(bloggerContext)
                : createDevelopmentRootDispatcher(bloggerContext);

        ContentRendererRegisty rendererRegistry = new ContentRendererRegisty()
                .register(new MarkdownRenderer());
        SimpleMimeParser mimeParser = new SimpleMimeParser();

        DispatcherContext baseContext =
                DispatcherContext.create(rootDispatcher, mimeParser, rendererRegistry, bloggerContext);
        NamespacedDispatcherContext dispatcherContext = NamespacedDispatcherContext.create(baseContext, rootNamespace,
                rootDispatcher);

        Spark.ipAddress(bconfig.getListeningIp());
        Spark.port(bconfig.getListeningPort());

        System.out.println("Spark Listening Path: " + sparkListenerPath);

        Spark.get(sparkListenerPath, (req, resp) -> {
            String spath = req.pathInfo();
            String[] path = dropHead(canonizePath(spath), rootNamespace);

            Object result = rootDispatcher.dispatch(path, req, resp, dispatcherContext);

            // Spark somehow instead of piping the content of InputStream, would
            // convert the InputStream to String with the JVM default encoding,
            // then serve the string in UTF-8, which is troublesome for our use case
            if (result instanceof InputStream) {
                // this is a hack to the Spark framework for custom serialization logic
                HttpServletResponse rresp = resp.raw();
                InputStream is = (InputStream) result;
                try (ServletOutputStream os = rresp.getOutputStream()) {
                    rresp.setStatus(200);
                    IOUtils.copy(is, os);
                }
            }

            return result;
        });

    }

    private static void logConfig(GitBloggerConfiguration config) {
        BaseConfig base = config.getBaseConfig();
        System.out.println(String.format(
                "Git Blogger started in %s mode", base.isProductionMode() ? "PRODUCTION" : "DEVELOPMENT"));
        System.out.println(String.format(
                "Default Source/Output Encoding: %s/%s",
                base.getDefaultSourceEncoding(), base.getDefaultOutputEncoding()));
        System.out.println(String.format(
                "Listening on %s:%d", base.getListeningIp(), base.getListeningPort()));
        System.out.println(String.format(
                "Canonical URL = %s",
                base.getCanonicalUrl().orElse("UNCONFIGURED")));

        BiConsumer<String, FSGitRepoConfig> describeRepoConfig =
                (prefix, conf) -> {
                    System.out.println(prefix + ": " + String.format(
                            "[ref=%s bare=%s gitDir=%s index=%s workingDir=%s]",
                            conf.getProductionExposedRef(),
                            conf.isBare(),
                            conf.getGitDir(),
                            conf.getIndexFile(),
                            conf.getWorkingDir()));
                };

        FSGitRepoConfig root = config.getRootRepoConfig();
        Map<String, FSGitRepoConfig> nonroots = config.getNonrootRepoConfigMap();

        describeRepoConfig.accept("Root repository: ", root);

        System.out.println(String.format("Nonroot repositories (count=%d):", nonroots.size()));
        nonroots.entrySet().stream()
        .forEach(e -> {
            describeRepoConfig.accept("    .... " + e.getKey() + ": ", e.getValue());
        });
    }

    private static NamespacedDispatcher createDevelopmentRootDispatcher(GitBloggerContext bloggerContext)
            throws IOException {
        ResourceDispatcher rawDispatcher = new RawDispatcher();
        ResourceDispatcher viewDispatcher = new ViewDispatcher();
        ResourceDispatcher browseDispatcher = new BrowseDispatcher();

        FSGitRepoConfig rootRepoConfig = bloggerContext.getConfig().getRootRepoConfig();
        Repository gitrepo = GitUtils.openGitRepository(rootRepoConfig);

        ChainNamespacedDispatcherBuilder<ChainNamespacedDispatcherBuilder<?>> rootDispatcherBuilder =
                NamespacedDispatcher.createBuilder()
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
            .chainForwarding(path
                    -> checkHead(path, of("exposed")) ? combine(of("index", "browse"), drop(path, 1)) : null);

        if (! rootRepoConfig.isBare()) {
            ResourceRepository wtrepo = new FileSystemResourceRepository(rootRepoConfig.getWorkingDir().get());
            ResourceRepository idxrepo = new GitIndexResourceRepository(DirCache.read(gitrepo),
                    gitrepo.newObjectReader());

            rootDispatcherBuilder
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
                    .finish();
        }

        ChainNamespacedDispacher rootDispatcher = rootDispatcherBuilder.buildFinal();
        return rootDispatcher;
    }

    private static NamespacedDispatcher createProductionRootDispatcher(GitBloggerContext bloggerContext)
            throws IOException {
        ResourceDispatcher rawDispatcher = new RawDispatcher();
        ResourceDispatcher viewDispatcher = new ViewDispatcher();
        ResourceDispatcher browseDispatcher = new BrowseDispatcher();

        FSGitRepoConfig rootRepoConfig = bloggerContext.getConfig().getRootRepoConfig();
        Repository gitrepo = GitUtils.openGitRepository(rootRepoConfig);
        String exposedRef = rootRepoConfig.getProductionExposedRef();

        ResourceRepository repo = GitRefResourceRepository.forRef(gitrepo, exposedRef);

        ChainNamespacedDispacher phantomDispatcher =
                NamespacedDispatcher.createBuilder()
            .withResourceRepository(repo)
                .dispatchLocation("raw", rawDispatcher)
                .dispatchLocation("view", viewDispatcher)
                .dispatchLocation("browse", browseDispatcher)
                .finish()
            .buildFinal();

        NamespacedDispatcher rootDispatcher = (path, req, resp, context) -> {
            String[] phantomPath = combine("browse", path);
            NamespacedDispatcherContext phantomContext = NamespacedDispatcherContext.create(
                    context.getParentContext(), of(), phantomDispatcher);
            return phantomDispatcher.dispatch(phantomPath, req, resp, phantomContext);
        };

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
            repo = GitRefResourceRepository.forRef(gitrepo, ref);
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
