package dev.rocky.rockylib.api.platform.v1;

/**
 * Creates a new ModInfo instance.
 *
 * @param id          the unique identifier for the mod
 * @param name        the human-readable name of the mod
 * @param version     the version of the mod
 * @param description a brief description of what the mod does
 */
public record ModInfo(String id, String name, String version, String description) {
}