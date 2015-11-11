package name.haochenxie.gitblogger;

import static spark.Spark.get;
import static spark.Spark.halt;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import name.haochenxie.gitblogger.dispatcher.BrowseDispatcher;
import name.haochenxie.gitblogger.dispatcher.RawDispatcher;
import name.haochenxie.gitblogger.dispatcher.ViewDispatcher;
import name.haochenxie.gitblogger.framework.ResourceRepository;
import name.haochenxie.gitblogger.framework.dispatcher.DispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherChainBuilder;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedDispatcherContext;
import name.haochenxie.gitblogger.framework.dispatcher.NamespacedPathDispatcherChain;
import name.haochenxie.gitblogger.framework.mime.MimeParser;
import name.haochenxie.gitblogger.framework.renderer.ContentRendererRegisty;
import name.haochenxie.gitblogger.framework.repo.FileSystemResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitIndexResourceRepository;
import name.haochenxie.gitblogger.framework.repo.GitTreeResourceRepository;
import name.haochenxie.gitblogger.mime.SimpleMimeParser;
import name.haochenxie.gitblogger.renderer.MarkdownRenderer;

public class GitBlogger {

    public static void main(String[] args) throws Exception {

        GitBloggerContext bloggerContext = GitBloggerContext.createDefault();

        MimeParser mimeParser = new SimpleMimeParser();

        ContentRendererRegisty rendererRegistry = new ContentRendererRegisty()
                .register(new MarkdownRenderer());

        Repository gitrepo = bloggerContext.openGitRepoOnRoot();

        Function<ResourceRepository, NamespacedPathDispatcherChain> chainCreator = repo ->
            new NamespacedDispatcherChainBuilder()
                .withRepository(repo)
                .addLocation("/raw", new RawDispatcher())
                .addLocation("/view", new ViewDispatcher())
                .addLocation("/browse", new BrowseDispatcher())
                .build();

        // TODO
        get("/refs/*", (req, resp) -> {
            String[] fullpath = req.splat()[0].split(":");
            String path = fullpath[1];
            String ref = fullpath[0];

            GitTreeResourceRepository refrepo = GitTreeResourceRepository.forRef(gitrepo, ref);
            NamespacedPathDispatcherChain chain = chainCreator.apply(refrepo);

            NamespacedDispatcherContext context = NamespacedDispatcherContext.create(
                    DispatcherContext.create(chain, mimeParser, rendererRegistry, bloggerContext), chain);

            return chain.dispatchPath(path, req, resp, context);
        });

        get("/tree/:objectid/*", (req, resp) -> {
            String path= req.splat()[0];
            String treeId = req.params("objectid");

            GitTreeResourceRepository refrepo = GitTreeResourceRepository.forTree(gitrepo, treeId);
            NamespacedPathDispatcherChain chain = chainCreator.apply(refrepo);

            NamespacedDispatcherContext context = NamespacedDispatcherContext.create(
                    DispatcherContext.create(chain, mimeParser, rendererRegistry, bloggerContext), chain);

            return chain.dispatchPath(path, req, resp, context);
        });

        get("/commit/:objectid/*", (req, resp) -> {
            String path= req.splat()[0];
            String commitId = req.params("objectid");

            GitTreeResourceRepository refrepo = GitTreeResourceRepository.forCommit(gitrepo, commitId);
            NamespacedPathDispatcherChain chain = chainCreator.apply(refrepo);

            NamespacedDispatcherContext context = NamespacedDispatcherContext.create(
                    DispatcherContext.create(chain, mimeParser, rendererRegistry, bloggerContext), chain);

            return chain.dispatchPath(path, req, resp, context);
        });

        get("/object/:objectid", (req, resp) -> {
            ObjectId objectId = ObjectId.fromString(req.params("objectid"));
            if (gitrepo.hasObject(objectId)) {
                Optional<String> asMime = Optional.ofNullable(req.queryParams("as"));
                resp.type(asMime.orElse("text/plain"));
                return gitrepo.getObjectDatabase().open(objectId).openStream();
            } else {
                halt(404, "sorry, i don't get it");
                return null;
            }
        });

        get("/worktree/*", (req, resp) -> {
            String path = req.splat()[0];

            FileSystemResourceRepository wtrepo = new FileSystemResourceRepository(bloggerContext.getRoot());
            NamespacedPathDispatcherChain chain = chainCreator.apply(wtrepo);

            NamespacedDispatcherContext context = NamespacedDispatcherContext.create(
                    DispatcherContext.create(chain, mimeParser, rendererRegistry, bloggerContext), chain);

            return chain.dispatchPath(path, req, resp, context);
        });

        get("/index/*", (req, resp) -> {
            String path = req.splat()[0];
            DirCache index = DirCache.read(gitrepo);
            GitIndexResourceRepository idxrepo = new GitIndexResourceRepository(index, gitrepo.newObjectReader());
            NamespacedPathDispatcherChain chain = chainCreator.apply(idxrepo);

            NamespacedDispatcherContext context = NamespacedDispatcherContext.create(
                    DispatcherContext.create(chain, mimeParser, rendererRegistry, bloggerContext), chain);

            return chain.dispatchPath(path, req, resp, context);
        });
    }


}
