package dev.rocky.rockylib.api.core;

import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated Use {@link dev.rocky.rockylib.api.core.v2.RockyLibInitializer} instead.
 */
@Deprecated
public class RockyLibMod {
    private final String ID;

    public RockyLibMod(String modId) {
        this.ID = modId;
    }

    public ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
