package name.haochenxie.gitblogger.framework.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import name.haochenxie.gitblogger.GitBloggerContext;
import name.haochenxie.gitblogger.RendererException;

public class ContentRendererRegisty implements ContentRenderer {

    private List<ContentRenderer> registry = Collections.synchronizedList(new LinkedList<>());

    public ContentRendererRegisty register(ContentRenderer renderer) {
        registry.add(renderer);
        return this;
    }

    public ContentRendererRegisty deregister(ContentRenderer renderer) {
        registry.remove(renderer);
        return this;
    }

    @Override
    public Set<String> getSupportedSourceMimeTypes() {
        return registry.stream()
            .reduce(Collections.<String>emptySet(),
                    (set, renderer) -> Sets.union(set, renderer.getSupportedSourceMimeTypes()),
                    (set1, set2) -> Sets.union(set1, set2));
    }

    @Override
    public String render(String sourceMime, InputStream source, OutputStream output, GitBloggerContext context)
            throws RendererException, IOException {
        Optional<ContentRenderer> renderer = registry.stream()
            .filter(r -> r.isSourceMimeTypeSupported(sourceMime))
            .findAny();

        String contentType = renderer
                .orElseThrow(() -> new RendererException("No suitable renderer is registered for type: " + sourceMime))
                .render(sourceMime, source, output, context);

        return contentType;
    }


}
