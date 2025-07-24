package dev.rocky.rockylib.api.registry.v1;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Handles registration into a specific registry.
 */
public interface Registrar<T> {
    /**
     * Registers a new value.
     */
    <R extends T> RegistrySupplier<R> register(ResourceLocation id, Supplier<? extends R> supplier);

    /**
     * Returns the key of the backing registry.
     */
    ResourceKey<? extends Registry<T>> key();

    /**
     * Retrieves an already registered value.
     */
    Optional<Holder.Reference<T>> get(ResourceLocation id);
}
