package name.haochenxie.gitblogger.framework.repo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheTree;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.google.common.base.Joiner;

import name.haochenxie.gitblogger.framework.ResourceRepository;

public class GitIndexResourceRepository implements ResourceRepository {

    private DirCache index;

    private ObjectReader objectReader;

    public GitIndexResourceRepository(DirCache index, ObjectReader objectReader) {
        this.index = index;
        this.objectReader = objectReader;
    }

    @Override
    public boolean checkExistence(String[] path) throws IOException {
        return checkIfResource(path) || checkIfTree(path);
    }

    @Override
    public InputStream open(String[] rpath) throws IOException {
        String path = Joiner.on('/').join(rpath);
        DirCacheEntry entry = Optional.ofNullable(index.getEntry(path)).orElseThrow(() -> new FileNotFoundException());
        return objectReader.open(entry.getObjectId()).openStream();
    }

    @Override
    public boolean checkIfTree(String[] path) throws IOException {
        DirCacheTree tree = index.getCacheTree(true);
        return checkIfTree(path, 0, tree);
    }

    private boolean checkIfTree(String[] rpath, int k, DirCacheTree tree) {
        if (k >= rpath.length) {
            return true;
        } else {
            for (int i = 0; i < tree.getChildCount(); ++i) {
                DirCacheTree subtree = tree.getChild(i);
                if (subtree.getNameString().equals(rpath[k])) {
                    return checkIfTree(rpath, k + 1, subtree);
                }
            }

            return false;
        }
    }

    @Override
    public boolean checkIfResource(String[] rpath) throws IOException {
        String path = Joiner.on('/').join(rpath);
        return ! (index.findEntry(path) < 0);
    }

    @Override
    public TreeListing createTreeListing(String[] treePath) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
