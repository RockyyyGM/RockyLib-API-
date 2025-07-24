package dev.rocky.rockylib.platform;

import dev.rocky.rockylib.api.registry.v1.Registrar;
import dev.rocky.rockylib.api.registry.v1.RegistrySupplier;
import dev.rocky.rockylib.platform.services.IRegistrarManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Supplier;

public class FabricRegistrarManager implements IRegistrarManager {
    @Override
    public <T> Registrar<T> create(String modId, ResourceKey<Registry<T>> key) {
        return new FabricRegistrar<>(key);
    }

    private static class FabricRegistrar<T> implements Registrar<T> {
        private final ResourceKey<Registry<T>> key;

        FabricRegistrar(ResourceKey<Registry<T>> key) {
            this.key = key;
        }

        @Override
        public <R extends T> RegistrySupplier<R> register(ResourceLocation id, Supplier<? extends R> supplier) {
            Registry<T> registry = registry();
            R value = supplier.get();
            Registry.register(registry, id, value);
            return new FabricRegistrySupplier<>(key.location(), id, value);
        }

        @SuppressWarnings("unchecked")
        private Registry<T> registry() {
            return (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key.location());
        }

        @Override
        public ResourceKey<? extends Registry<T>> key() {
            return key;
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceLocation id) {
            return registry().get(id);
        }
    }

    private static class FabricRegistrySupplier<R> implements RegistrySupplier<R> {
        private final ResourceLocation registryId;
        private final ResourceLocation id;
        private final R value;

        FabricRegistrySupplier(ResourceLocation registryId, ResourceLocation id, R value) {
            this.registryId = registryId;
            this.id = id;
            this.value = value;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public R get() {
            return value;
        }

        @Override
        public ResourceLocation getRegistryId() {
            return registryId;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }
    }
}
