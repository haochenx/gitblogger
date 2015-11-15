package name.haochenxie.gitblogger.framework.repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.google.common.base.Joiner;

import name.haochenxie.gitblogger.framework.ResourceRepository;

public class GitTreeResourceRepository implements ResourceRepository {

    private RevTree root;

    protected ObjectReader objectReader;

    public GitTreeResourceRepository(RevTree root, ObjectReader objectReader) {
        this.root = root;
        this.objectReader = objectReader;
    }

    public static GitTreeResourceRepository forTree(Repository repo, String treeId) throws IOException {
        ObjectReader or = repo.newObjectReader();

        try (RevWalk walk = new RevWalk(or)) {
            RevTree tree = walk.lookupTree(ObjectId.fromString(treeId));
            return new GitTreeResourceRepository(tree, or);
        }
    }

    public static GitTreeResourceRepository forCommit(Repository repo, String commitId) throws IOException {
        ObjectReader or = repo.newObjectReader();

        try (RevWalk walk = new RevWalk(or)) {
            walk.setRetainBody(false);
            RevCommit commit = walk.parseCommit(ObjectId.fromString(commitId));
            RevTree tree = commit.getTree();

            return new GitTreeResourceRepository(tree, or);
        }
    }

    @Override
    public boolean checkExistence(String[] rpath) throws IOException {
        ensureUpdated();
        try (TreeWalk walk = new TreeWalk(objectReader)) {
            if (rpath.length == 0) {
                return true;
            }

            walk.setRecursive(true);
            walk.addTree(root);
            String path = Joiner.on('/').join(rpath);
            walk.setFilter(PathFilter.create(path));

            while (walk.next()) {
                if (Objects.equals(walk.getPathString(), path)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public InputStream open(String[] rpath) throws IOException {
        ensureUpdated();
        try (TreeWalk walk = new TreeWalk(objectReader)) {
            walk.setRecursive(true);
            walk.addTree(root);
            String path = Joiner.on('/').join(rpath);
            walk.setFilter(PathFilter.create(path));

            while (walk.next()) {
                if (Objects.equals(walk.getPathString(), path) && ! walk.isSubtree()) {
                    return objectReader.open(walk.getObjectId(0)).openStream();
                }
            }

            throw new FileNotFoundException("path is not an existing blob in the repository: " + path);
        }
    }

    @Override
    public boolean checkIfTree(String[] rpath) throws IOException {
        ensureUpdated();
        try (TreeWalk walk = new TreeWalk(objectReader)) {
            if (rpath.length == 0) {
                return true;
            }

            walk.setRecursive(true);
            walk.addTree(root);
            String path = Joiner.on('/').join(rpath);
            walk.setFilter(PathFilter.create(path));

            while (walk.next()) {
                if (Objects.equals(walk.getPathString(), path) &&  walk.isSubtree()) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean checkIfResource(String[] rpath) throws IOException {
        ensureUpdated();
        try (TreeWalk walk = new TreeWalk(objectReader)) {
            if (rpath.length == 0) {
                return false;
            }

            walk.setRecursive(true);
            walk.addTree(root);
            String path = Joiner.on('/').join(rpath);
            walk.setFilter(PathFilter.create(path));

            while (walk.next()) {
                if (Objects.equals(walk.getPathString(), path) &&  ! walk.isSubtree()) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public TreeListing createTreeListing(String[] rpath) throws IOException {
        String path = Joiner.on('/').join(rpath);

        return new TreeListing() {

            @Override
            public Collection<String> listChildrenTrees() throws IOException {
                List<String> list = new ArrayList<>();

                ensureUpdated();
                try (TreeWalk walk = new TreeWalk(objectReader)) {
                    walk.setRecursive(false);
                    walk.addTree(root);
                    walk.setFilter(PathFilter.create(path));

                    while (walk.next()) {
                        String cpath = walk.getPathString();
                        if (path.startsWith(cpath)) {
                            walk.enterSubtree();
                        } else if (cpath.startsWith(path) && walk.isSubtree()) {
                            list.add(walk.getNameString());
                        }
                    }
                }

                return list;
            }

            @Override
            public Collection<String> listChidrenResources() throws IOException {
                List<String> list = new ArrayList<>();

                ensureUpdated();
                try (TreeWalk walk = new TreeWalk(objectReader)) {
                    walk.setRecursive(false);
                    walk.addTree(root);
                    walk.setFilter(PathFilter.create(path));

                    while (walk.next()) {
                        String cpath = walk.getPathString();
                        if (path.startsWith(cpath)) {
                            walk.enterSubtree();
                        } else if (cpath.startsWith(path) && ! walk.isSubtree()) {
                            list.add(walk.getNameString());
                        }
                    }
                }

                return list;
            }
        };
    }

    protected void ensureUpdated() throws IOException {
        // no-op
    }

    protected RevTree getRootTree() {
        return root;
    }

    protected void setRootTree(RevTree root) {
        this.root = root;
    }

}
