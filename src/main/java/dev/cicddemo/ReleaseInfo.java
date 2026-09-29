package dev.cicddemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record ReleaseInfo(String name, String message, String version, String commit, String build) {
    public ReleaseInfo {
        for (String value : new String[]{name, message, version, commit, build}) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Release fields must not be blank");
            }
        }
    }

    public static ReleaseInfo load() throws IOException {
        try (InputStream stream = ReleaseInfo.class.getResourceAsStream("/release.properties")) {
            if (stream == null) throw new IOException("Missing release.properties");
            Properties properties = new Properties();
            properties.load(stream);
            return from(properties);
        }
    }

    public static ReleaseInfo from(Properties properties) {
        return new ReleaseInfo(properties.getProperty("app.name"), properties.getProperty("app.message"),
                properties.getProperty("app.version"), properties.getProperty("git.commit"),
                properties.getProperty("build.number"));
    }

    public String shortCommit() {
        return commit.substring(0, Math.min(12, commit.length()));
    }

    public static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
