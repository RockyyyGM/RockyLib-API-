package dev.rocky.rockylib.platform.services;

import dev.rocky.rockylib.api.registry.v1.Registrar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Service providing loader specific registrar implementations.
 */
public interface IRegistrarManager {
    /**
     * Creates a registrar for the given registry and mod id.
     */
    <T> Registrar<T> create(String modId, ResourceKey<Registry<T>> key);
}
