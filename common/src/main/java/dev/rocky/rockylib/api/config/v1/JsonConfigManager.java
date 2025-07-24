package dev.rocky.rockylib.api.config.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import dev.rocky.rockylib.Constants;
import dev.rocky.rockylib.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO: Add backup, value transformation, and other features similar to JsonFileReader.

/**
 * A static JSON configuration manager that reads and writes configuration files in JSON format.
 * The configuration file is expected to be in JSON format and can be read and written using Gson.
 * @param <T> the type of the configuration object
 * @since 6.0.1
 */
public class JsonConfigManager<T> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final T initialConfig;
    private final Path configPath;
    private final String modId;
    private String headerComment = "";
    private T config;

    /**
     * Constructs a new StaticJsonConfig instance.
     *
     * @param modId         the mod ID for which this config is being created
     * @param initialConfig the initial configuration object, which will be used if the config file does not exist
     * @param configPath    the path to the configuration file; if null, defaults to the mod ID with ".json5" extension in the config directory
     * @param headerComment an optional header comment to include in the configuration file
     */
    public JsonConfigManager(String modId, T initialConfig, @Nullable Path configPath, @Nullable String headerComment) {
        this.modId = modId;
        this.headerComment = headerComment;
        this.config = initialConfig;
        this.initialConfig = initialConfig;
        this.configPath = configPath == null ? Services.PLATFORM.getConfigDirectory().resolve(modId + ".json5") : configPath;
    }

    /**
     * Gets the current configuration object.
     * If the configuration has not been loaded yet, it will load it from the file.
     *
     * @return the current configuration object
     */
    public T getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * Loads the configuration from the specified file.
     * If the file does not exist, it creates a new file with the initial configuration.
     * If an error occurs while reading the file, it logs the error and uses the initial configuration.
     */
    public void loadConfig() {
        try {
            if (Files.exists(configPath)) {
                try (JsonReader reader = new JsonReader(Files.newBufferedReader(configPath))) {
                    // this line could be different based on the version of Gson being used
                    reader.setStrictness(Strictness.LEGACY_STRICT);
                    config = GSON.fromJson(reader, initialConfig.getClass());
                }
            } else {
                saveConfig();
                Constants.LOG.info("Created default configuration at {} for mod {}.", configPath.toAbsolutePath(), modId);
            }
        } catch (Exception e) {
            Constants.LOG.error(
                    "Could not read config at {} for mod {}. Using defaults. Please check the file for errors.",
                    configPath == null ? "<unknown>" : configPath.toAbsolutePath(),
                    modId
            );
            Constants.LOG.error("{}", e.getMessage());
            config = initialConfig;
        }
    }

    /**
     * Saves the current configuration to the specified file.
     * If the directory does not exist, it creates the necessary directories.
     * If an error occurs while writing the file, it logs the error.
     */
    public void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                writer.write(headerComment);
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            Constants.LOG.error(
                    "Failed to save config to {} for mod {}. Changes may be lost.",
                    configPath.toAbsolutePath(),
                    modId
            );
            Constants.LOG.error("{}", e.getMessage());
        }
    }
}
