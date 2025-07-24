package dev.rocky.rockylib.api.registry.v1;

import dev.rocky.rockylib.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple manager for registry access.
 */
public final class RegistrarManager {
    private static final Map<String, RegistrarManager> MANAGER = new HashMap<>();

    private final String modId;
    private final Map<ResourceKey<? extends Registry<?>>, Registrar<?>> registrars = new HashMap<>();

    private RegistrarManager(String modId) {
        this.modId = modId;
    }

    /**
     * Obtains the manager for the given mod id.
     */
    public static RegistrarManager get(String modId) {
        return MANAGER.computeIfAbsent(modId, RegistrarManager::new);
    }

    @SuppressWarnings("unchecked")
    public <T> Registrar<T> get(ResourceKey<Registry<T>> key) {
        return (Registrar<T>) registrars.computeIfAbsent(key, k -> Services.REGISTRAR_MANAGER.create(modId, key));
    }

    public String getModId() {
        return modId;
    }

}
