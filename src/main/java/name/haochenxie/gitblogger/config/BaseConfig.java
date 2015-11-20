package name.haochenxie.gitblogger.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public interface BaseConfig {

    public boolean isProductionMode();

    public Charset getDefaultSourceEncoding();

    public Charset getDefaultOutputEncoding();

    public String getListeningIp();

    public int getListeningPort();

    public Optional<String> getCanonicalUrl();

    public static BaseConfig parseConfig(Properties prop) {
        boolean isProductionMode = prop.getProperty("gitblogger.production") != null;
        String ip = prop.getProperty("gitblogger.ip", "0.0.0.0");
        Optional<Integer> port =
                Optional.ofNullable(prop.getProperty("gitblogger.port")).map(str -> Integer.parseInt(str));
        Optional<String> canonicalUrl = Optional.ofNullable(prop.getProperty("gitblogger.canonicalUrl"));

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