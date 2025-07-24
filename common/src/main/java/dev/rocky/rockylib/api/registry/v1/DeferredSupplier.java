package dev.rocky.rockylib.api.registry.v1;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Information about an entry that may not yet be registered.
 */
public interface DeferredSupplier<T> extends OptionalSupplier<T> {
    /**
     * Returns the identifier of the registry this supplier belongs to.
     */
    ResourceLocation getRegistryId();

    /**
     * Returns the registry key for this supplier.
     */
    default ResourceKey<Registry<T>> getRegistryKey() {
        return ResourceKey.createRegistryKey(getRegistryId());
    }

    /**
     * Returns the identifier of the entry.
     */
    ResourceLocation getId();

    /**
     * Returns the entry key.
     */
    default ResourceKey<T> getKey() {
        return ResourceKey.create(getRegistryKey(), getId());
    }
}
