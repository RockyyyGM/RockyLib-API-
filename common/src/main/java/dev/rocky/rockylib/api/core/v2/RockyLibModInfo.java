package dev.rocky.rockylib.api.core.v2;

import org.jetbrains.annotations.Nullable;

/**
 * Represents information about an RockyLib mod.
 *
 * @param id      The unique identifier of the mod.
 * @param name    The human-readable name of the mod.
 * @param version The version of the mod.
 * @param side    The side the mod is intended for (common, client, or server).
 * @param eventBus An optional event bus object for the mod, can be null.
 */
public record RockyLibModInfo(String id, String name, String version,
                              dev.rocky.rockylib.api.core.v2.RockyLibModInfo.RockyLibModSide side, @Nullable Object eventBus) {

    public enum RockyLibModSide {
        COMMON,
        CLIENT,
        SERVER
    }
}
