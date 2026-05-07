package pl.fuzjajadrowa.locatorbar.config;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class LocatorBarServerConfig {
    private static final Path CONFIG_PATH = Path.of("config", "locatorbar-server.toml");
    private static ServerSettings data = ServerSettings.defaults();

    private LocatorBarServerConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(new TomlPropertiesReader(reader));
            data = new ServerSettings(
                    readStyle(properties, "style", LocatorBarStyle.REWORKED),
                    readBoolean(properties, "showCoordinates", true),
                    readBoolean(properties, "showDays", false),
                    readBoolean(properties, "showWorldDirections", true),
                    readBoolean(properties, "showPlayerHeads", true),
                    readInt(properties, "maxVisiblePlayers", 16, 1, 64),
                    readBoolean(properties, "showWaypoints", true),
                    readInt(properties, "maxVisibleWaypoints", 16, 1, 64)
            );
        } catch (IOException | IllegalArgumentException exception) {
            data = ServerSettings.defaults();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                writer.write("# Locator Bar server-enforced settings\n");
                writer.write("style = \"" + data.style().name().toLowerCase(Locale.ROOT) + "\"\n");
                writer.write("showCoordinates = " + data.showCoordinates() + "\n");
                writer.write("showDays = " + data.showDays() + "\n");
                writer.write("showWorldDirections = " + data.showWorldDirections() + "\n");
                writer.write("showPlayerHeads = " + data.showPlayerHeads() + "\n");
                writer.write("maxVisiblePlayers = " + data.maxVisiblePlayers() + "\n");
                writer.write("showWaypoints = " + data.showWaypoints() + "\n");
                writer.write("maxVisibleWaypoints = " + data.maxVisibleWaypoints() + "\n");
            }
        } catch (IOException ignored) {
            // Keep startup stable even if saving fails.
        }
    }

    public static ServerSettings get() {
        return data;
    }

    private static LocatorBarStyle readStyle(Properties properties, String key, LocatorBarStyle fallback) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }

        try {
            return LocatorBarStyle.valueOf(value.trim().replace("\"", "").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static int readInt(Properties properties, String key, int fallback, int min, int max) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public record ServerSettings(
            LocatorBarStyle style,
            boolean showCoordinates,
            boolean showDays,
            boolean showWorldDirections,
            boolean showPlayerHeads,
            int maxVisiblePlayers,
            boolean showWaypoints,
            int maxVisibleWaypoints
    ) {
        public static ServerSettings defaults() {
            return new ServerSettings(LocatorBarStyle.REWORKED, true, false, true, true, 16, true, 16);
        }
    }

    private static final class TomlPropertiesReader extends Reader {
        private final Reader delegate;
        private String content;
        private int index;

        private TomlPropertiesReader(Reader delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (content == null) {
                content = normalize(delegate);
            }
            if (index >= content.length()) {
                return -1;
            }

            int count = Math.min(len, content.length() - index);
            content.getChars(index, index + count, cbuf, off);
            index += count;
            return count;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        private static String normalize(Reader reader) throws IOException {
            StringBuilder output = new StringBuilder();
            StringBuilder line = new StringBuilder();
            int read;
            while ((read = reader.read()) >= 0) {
                char character = (char) read;
                if (character == '\n') {
                    appendLine(output, line.toString());
                    line.setLength(0);
                } else if (character != '\r') {
                    line.append(character);
                }
            }
            appendLine(output, line.toString());
            return output.toString();
        }

        private static void appendLine(StringBuilder output, String line) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                return;
            }
            output.append(trimmed.replaceFirst("\\s*=\\s*", "=")).append('\n');
        }
    }
}