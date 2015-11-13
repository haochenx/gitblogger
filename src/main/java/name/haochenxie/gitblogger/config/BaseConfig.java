package name.haochenxie.gitblogger.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface BaseConfig {

    public boolean isProductionMode();

    public Charset getDefaultSourceEncoding();

    public Charset getDefaultOutputEncoding();

    public String getListeningIp();

    public int getListeningPort();

    public Optional<String> getCanonicalUrl();

    public static BaseConfig getCurrentConfig() {
        boolean isProductionMode = System.getProperty("gitblogger.production") != null;
        String ip = System.getProperty("gitblogger.listeningIp", "0.0.0.0");
        Optional<Integer> port =
                Optional.ofNullable(System.getProperty("gitblogger.listeningPort")).map(str -> Integer.parseInt(str));
        Optional<String> canonicalUrl = Optional.ofNullable(System.getProperty("gitblogger.canonicalUrl"));
    
        return create(isProductionMode, StandardCharsets.UTF_8, StandardCharsets.UTF_8, ip, port.orElse(4567),
                canonicalUrl);
    }

    public static BaseConfig create(boolean isProductionMode, Charset defaultSourceEncoding,
            Charset defaultOutputEncoding, String ip, int port, Optional<String> canonicalUrl) {
        return new BaseConfig() {

            @Override
            public boolean isProductionMode() {
                return isProductionMode;
            }

            @Override
            public String getListeningIp() {
                return ip;
            }

            @Override
            public int getListeningPort() {
                return port;
            }

            @Override
            public Charset getDefaultSourceEncoding() {
                return defaultSourceEncoding;
            }

            @Override
            public Charset getDefaultOutputEncoding() {
                return defaultOutputEncoding;
            }

            @Override
            public Optional<String> getCanonicalUrl() {
                return canonicalUrl;
            }
        };
    }

}