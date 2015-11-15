package name.haochenxie.gitblogger.framework.repo;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;

public class GitRefResourceRepository extends GitTreeResourceRepository {

    private String ref;

    private Repository repo;

    public GitRefResourceRepository(ObjectReader or, Repository repo, String ref) throws IOException {
        super(openTree(or, repo, ref), or);
        this.repo = repo;
        this.ref = ref;
    }

    @Override
    protected void ensureUpdated() throws IOException {
        setRootTree(openTree(objectReader, repo, ref));
    }

    private static RevTree openTree(ObjectReader or, Repository repo, String ref) throws IOException {
        ObjectId rev = repo.getRef(ref).getLeaf().getObjectId();

        try (RevWalk walk = new RevWalk(or)) {
            walk.setRetainBody(false);
            RevCommit commit = walk.parseCommit(rev);
            RevTree tree = commit.getTree();

            return tree;
        }
    }

    public static GitTreeResourceRepository forRef(Repository repo, String ref) throws IOException {
        ObjectReader or = repo.newObjectReader();
        return new GitRefResourceRepository(or, repo, ref);
    }

}