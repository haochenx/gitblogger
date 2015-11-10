package name.haochenxie.gitblogger.framework.mime;

public interface MimeParser {

    /**
     * judge the MIME type with only the resource's preferred base name, which
     * basically means an extension -> MIME conversion
     *
     * @param baseName
     *            the base name (i.e. without any directory information) of the
     *            resource
     * @return the MIME type string for the resource
     */
    public String parseMime(String baseName);

}
