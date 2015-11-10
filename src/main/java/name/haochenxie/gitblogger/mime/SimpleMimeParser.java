package name.haochenxie.gitblogger.mime;

import name.haochenxie.gitblogger.framework.mime.ExtensionMimeParser;

public class SimpleMimeParser extends ExtensionMimeParser {

    {
        reconfigure();
    }

    public void reconfigure() {
        clearRegistry();
        createModification()
            .add("txt", "text/plain")
            .map("text/html", "html", "htm")
            .add("md", "text/markdown")
            .add("java", "text/x-java-source")

            .commit();
    }

    @Override
    public String parseMime(String baseName) {
        return super.parseMime(baseName);
    }

}
