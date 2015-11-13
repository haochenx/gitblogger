package name.haochenxie.gitblogger.framework.repo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

import name.haochenxie.gitblogger.framework.ResourceRepository;

public class FileSystemResourceRepository implements ResourceRepository {

    private File root;

    public FileSystemResourceRepository(File root) {
        this.root = root;
    }

    @Override
    public boolean checkExistence(String[] path) throws IOException {
        File f = toFile(path);
        return f.exists();
    }

    @Override
    public InputStream open(String[] resourcePath) throws IOException {
        try {
            File f = toFile(resourcePath);
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));
            return input;
        } catch (FileNotFoundException | AccessControlException ex) {
            throw new IOException("unable to open resource at " + Arrays.toString(resourcePath), ex);
        }
    }

    @Override
    public boolean checkIfTree(String[] path) throws IOException {
        File f = toFile(path);
        return f.isDirectory();
    }

    @Override
    public boolean checkIfResource(String[] path) throws IOException {
        File f = toFile(path);
        return f.isFile();
    }

    @Override
    public TreeListing createTreeListing(String[] treePath) throws IOException {
        File f = toFile(treePath);
        if (f.isDirectory()) {
            return new TreeListing() {

                @Override
                public Collection<String> listChildrenTrees() throws IOException {
                    List<String> list = Stream.of(f.listFiles())
                        .filter(file -> file.isDirectory())
                        .map(file -> file.getName())
                        .collect(Collectors.toList());
                    return list;
                }

                @Override
                public Collection<String> listChidrenResources() throws IOException {
                    List<String> list = Stream.of(f.listFiles())
                            .filter(file -> file.isFile())
                            .map(file -> file.getName())
                            .collect(Collectors.toList());
                        return list;
                }
            };
        } else {
            throw new IOException("unable to create listing of non-treeish at " + Arrays.toString(treePath));
        }

    }

    private File toFile(String[] respath) {
        String path = Joiner.on('/').join(respath);
        return new File(root, path);
    }

}
