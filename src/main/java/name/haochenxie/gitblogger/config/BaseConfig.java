package name.haochenxie.gitblogger.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface BaseConfig {

    public static String CONFKEY_PRODUCTION = "gitblogger.production";
    public static String CONFKEY_LISTENING_IP = "gitblogger.listeningIp";
    public static String CONFKEY_LISTENING_PORT = "gitblogger.listeningPort";
    public static String CONFKEY_CANONICAL_URL = "gitblogger.canonicalUrl";

    public static int DEFAULT_LISTENING_PORT = 4567;
    public static String DEFAULT_LISTENING_IP = "0.0.0.0";

    public boolean isProductionMode();

    public Charset getDefaultSourceEncoding();

    public Charset getDefaultOutputEncoding();

    public String getListeningIp();

    public int getListeningPort();

    public Optional<String> getCanonicalUrl();

    public static BaseConfig getCurrentConfig() {
        boolean isProductionMode = System.getProperty(CONFKEY_PRODUCTION) != null;
        String ip = System.getProperty(CONFKEY_LISTENING_IP, DEFAULT_LISTENING_IP);
        Optional<Integer> port =
                Optional.ofNullable(System.getProperty(CONFKEY_LISTENING_PORT)).map(str -> Integer.parseInt(str));
        Optional<String> canonicalUrl = Optional.ofNullable(System.getProperty(CONFKEY_CANONICAL_URL));
    
        return create(isProductionMode, StandardCharsets.UTF_8, StandardCharsets.UTF_8, ip, port.orElse(DEFAULT_LISTENING_PORT),
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