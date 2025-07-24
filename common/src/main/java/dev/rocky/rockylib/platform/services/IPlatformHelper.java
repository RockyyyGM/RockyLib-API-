package dev.rocky.rockylib.platform.services;

import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.util.Env;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Returns the root directory for configuration files.
     *
     * @return The config directory path.
     */
    java.nio.file.Path getConfigDirectory();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Gets the current environment type.
     *
     * @return The current environment type.
     */
    Env getEnvironment();

    /**
     * Gets the IDs of all loaded mods.
     *
     * @return A collection of mod IDs.
     */
    Collection<String> getModIds();

    /**
     * Gets the mod information for a mod.
     *
     * @return The mod information.
     */
    @Nullable ModInfo getModInfo(String modId);
}