package name.haochenxie.gitblogger.framework.mime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public class ExtensionMimeParser implements MimeParser {

    public static final String MIME_FOR_UNKNOWN = "application/octet-stream";

    public Map<String, String> extensionRegistry = new ConcurrentHashMap<>();

    @Override
    public String parseMime(String baseName) {
        String ext = FilenameUtils.getExtension(baseName);
        Optional<String> mime = Optional.ofNullable(extensionRegistry.get(ext));

        return mime.orElse(MIME_FOR_UNKNOWN);
    }

    public void clearRegistry() {
        extensionRegistry.clear();
    }

    public Modifier createModification() {
        return new Modifier();
    }

    public class Modifier {

        private Map<String, String> stage = new HashMap<>();

        public Modifier add(String ext, String mime) {
            stage.put(ext, mime);
            return this;
        }

        public Modifier map(String mime, String... exts) {
            Stream.of(exts)
                .forEach(ext -> add(ext, mime));
            return this;
        }

        public void commit() {
            extensionRegistry.putAll(stage);
            stage.clear();
        }

    }

}
