package name.haochenxie.gitblogger;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
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
import name.haochenxie.gitblogger.framework.mime.MimeUtils;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.framework.repo.FileSystemResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitIndexResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitRefResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitTreeResourceRepository;
import name.haochenxie.gitblogger.framework.util.GitUtils;
import name.haochenxie.gitblogger.framework.util.SuperOptional;
import name.haochenxie.gitblogger.mime.SimpleMimeParser;
import name.haochenxie.gitblogger.renderer.MarkdownRenderer;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;
import spark.Spark;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static name.haochenxie.gitblogger.framework.util.UriUtils.*;

@SuppressWarnings({"Convert2MethodRef", "UnnecessaryLocalVariable", "UnusedParameters"})
public class GitBlogger {

    public static void main(String[] args) throws Exception {
        parseArguments(args);

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

        // Private Repo Browser prototype
        {
            FSGitRepoConfig rootRepoConfig = bloggerContext.getConfig().getRootRepoConfig();
            Repository gitrepo = GitUtils.openGitRepository(rootRepoConfig);
            String exposedRef = rootRepoConfig.getProductionExposedRef();

            ResourceRepository repo = GitRefResourceRepository.forRef(gitrepo, exposedRef);

            Spark.get("/*", (req, resp) -> {
                String[] rpath = canonizePath(req.pathInfo());

                if (repo.checkExistence(rpath)) {
                    if (repo.checkIfTree(rpath)) {
                        ResourceRepository.TreeListing treeListing = repo.createTreeListing(rpath);

                        Collection<String> subtrees = treeListing.listChildrenTrees();
                        Collection<String> resources = treeListing.listChidrenResources();

                        List<String> children = Stream.concat(
                                subtrees.stream()
                                        .map(str -> str + "/"),
                                resources.stream())
                                .collect(toList());

                        if (rpath.length > 0) {
                            children = Stream.concat(
                                    Stream.of("./", "../"),
                                    children.stream()
                            ).collect(toList());
                        }

                        List<String> listHtml = children.stream()
                                .map(str -> String.format("<a href='%s'>%s</a>",
                                        "./" + str, str))
                                .collect(toList());

                        return Joiner.on("<br/>\n").join(listHtml);
                    } else if (repo.checkIfResource(rpath)) {
                        String basename = ResourceRepository.Helper.getBasename(rpath);
                        String mime = dispatcherContext.getMimeParser().parseMime(basename);

                        String contentType = mime;

                        // when serving 'text/*' mime types, it is advisable to specify the encoding.
                        // ref: RFC6657
                        // ref: http://www.iana.org/assignments/media-types/media-types.xhtml#text
                        if (mime.startsWith("text")) {
                            contentType = MimeUtils.constructContentType(mime,
                                    bconfig.getDefaultSourceEncoding());
                        }

                        resp.type(contentType);
                        BufferedInputStream input = new BufferedInputStream(repo.open(rpath));
                        return input;
                    } else {
                        String msgUncategorizable = "wow, resource exists but category unrecognized??!";
                        return msgUncategorizable;
                    }
                } else {
                    String msg404 = "wow, 404!";
                    return msg404;
                }
            });

        }

//        Spark.get(sparkListenerPath, (req, resp) -> {
//            String spath = req.pathInfo();
//            String[] path = dropHead(canonizePath(spath), rootNamespace);
//
//            Object result = rootDispatcher.dispatch(path, req, resp, dispatcherContext);
//
//            // Spark somehow instead of piping the content of InputStream, would
//            // convert the InputStream to String with the JVM default encoding,
//            // then serve the string in UTF-8, which is troublesome for our use case
//            if (result instanceof InputStream) {
//                // this is a hack to the Spark framework for custom serialization logic
//                HttpServletResponse rresp = resp.raw();
//                InputStream is = (InputStream) result;
//                try (ServletOutputStream os = rresp.getOutputStream()) {
//                    rresp.setStatus(200);
//                    IOUtils.copy(is, os);
//                }
//            }
//
//            return result;
//        });

    }

    private static void parseArguments(String[] args) {
        // flags: -development -production -bare
        // configurable: -ip -port -url
        // configurable: -repo -ref
        // default configurable: -repo
        // command: -help

        System.err.println("To see available options, run GitBlogger with -help");

        // productionMode : mutually exclusively controlled by -development and -production
        boolean productionMode = false;

        // bare : controlled by -bare
        boolean bare = false;

        // repo : configurable with identifier -repo
        String repo = new File(".").getAbsolutePath();

        String currentConfigurable = null;
        Set<String> configurables = Sets.newHashSet("ip", "port", "url", "repo", "ref");

        // allow "=" as a separator so that arguments like "-url=/blog" would be legal
        args = Arrays.stream(args)
                .flatMap(arg -> Arrays.stream(arg.split("=")))
                .toArray(String[]::new);

        for (String arg : args) {
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                switch (arg) {
                    case "development": productionMode = false; break;
                    case "production":  productionMode = true;  break;
                    case "bare":        bare = true;            break;

                    case "help":
                        System.err.println("GitBlogger https://github.com/haochenx/gitblogger");
                        System.err.println("Usage: gitblogger [[-repo] <git repository>] [-bare] [-production|-development]");
                        System.err.println("                  [-ip] [-port] [-url] [-ref <exposed ref>]");
                        System.err.println("       gitblogger -help");
                        System.exit(-1);

                    default:
                        if (configurables.contains(arg)) {
                            currentConfigurable = arg;
                        } else {
                            System.err.println("Unknown argument: " + arg);
                            System.exit(-1);
                        }
                }
            } else {
                currentConfigurable = currentConfigurable == null ? "repo" : currentConfigurable;
                switch (currentConfigurable) {
                    case "ip":   System.setProperty(BaseConfig.CONFKEY_LISTENING_IP, arg); break;
                    case "port": System.setProperty(BaseConfig.CONFKEY_LISTENING_PORT, arg); break;
                    case "url":  System.setProperty(BaseConfig.CONFKEY_CANONICAL_URL, arg); break;
                    case "repo": repo = arg; break;
                    case "ref":  System.setProperty(FSGitRepoConfig.CONFKEY_EXPOSED_REF, arg); break;

                    default:
                        throw new RuntimeException("Hmm, it seems that I missed to implement this flag! "
                                + currentConfigurable);
                }

                currentConfigurable = null;
            }
        }

        System.setProperty(bare ? FSGitRepoConfig.CONFKEY_ROOT_REPO_BARE : FSGitRepoConfig.CONFKEY_ROOT_REPO,
                repo);
        if (productionMode) {
            System.setProperty(BaseConfig.CONFKEY_PRODUCTION, "true");
        } else {
            System.clearProperty(BaseConfig.CONFKEY_PRODUCTION);
        }
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

        FSGitRepoConfig root = config.getRootRepoConfig();
        System.out.println(String.format(
                "Root repository bare? %s", root.isBare()));
        System.out.println(String.format(
                "    .... Git Dir     = %s", root.getGitDir().getAbsolutePath()));
        System.out.println(String.format(
                "    .... Working Dir = %s", root.getWorkingDir().map(f -> f.getAbsolutePath()).orElse("UNCONFIGURED")));
        System.out.println(String.format(
                "    .... Index File  = %s", root.getIndexFile().map(f -> f.getAbsolutePath()).orElse("UNCONFIGURED")));
        System.out.println(String.format(
                "    .... Exposed Ref = %s", root.getProductionExposedRef()));
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
