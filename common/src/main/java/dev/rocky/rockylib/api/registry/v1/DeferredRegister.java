package dev.rocky.rockylib.api.registry.v1;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simplified deferred register that queues entries for later registration.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public static final DeferredRegister<Item> ITEMS =
 *         DeferredRegister.create("examplemod", Registries.ITEM);
 *
 * public static final RegistrySupplier<Item> TEST_ITEM =
 *         ITEMS.register("test_item", key -> new Item(new Item.Properties().setId(key)));
 *
 * public static void init() {
 *     ITEMS.register(); // registers all queued items
 * }
 * }</pre>
 *
 * <p>The {@code ResourceKey} parameter can be used to set the id on any
 * {@code Properties} instance that requires it.</p>
 */
public class DeferredRegister<T> implements Iterable<RegistrySupplier<T>> {
    private final Supplier<RegistrarManager> managerSupplier;
    private final ResourceKey<Registry<T>> key;
    private final List<Entry<T>> entries = new ArrayList<>();
    private final List<RegistrySupplier<T>> entryView = Collections.unmodifiableList(entries);
    private boolean registered = false;
    private final String modId;

    private DeferredRegister(Supplier<RegistrarManager> managerSupplier, ResourceKey<Registry<T>> key, String modId) {
        this.managerSupplier = Objects.requireNonNull(managerSupplier);
        this.key = Objects.requireNonNull(key);
        this.modId = modId;
    }

    /**
     * Creates a deferred register bound to the given mod id and registry.
     */
    public static <T> DeferredRegister<T> create(String modId, ResourceKey<Registry<T>> key) {
        Supplier<RegistrarManager> value = Suppliers.memoize(() -> RegistrarManager.get(modId));
        return new DeferredRegister<>(value, key, modId);
    }

    /**
     * Registers a supplier with an id under the mod namespace.
     * Use this when the entry's {@code Properties} already have the id set.
     */
    public <R extends T> RegistrySupplier<R> register(String id, Supplier<? extends R> supplier) {
        if (modId == null) {
            throw new NullPointerException("DeferredRegister created without mod id");
        }
        return register(ResourceLocation.fromNamespaceAndPath(modId, id), supplier);
    }

    /**
     * Registers a supplier that receives the entry {@link ResourceKey}. This is useful for
     * automatically setting the id on {@code Properties} objects.
     */
    public <R extends T> RegistrySupplier<R> register(String id, Function<ResourceKey<T>, ? extends R> supplier) {
        if (modId == null) {
            throw new NullPointerException("DeferredRegister created without mod id");
        }
        return register(ResourceLocation.fromNamespaceAndPath(modId, id), supplier);
    }

    /**
     * Registers a supplier with a full id. Use when the constructed entry already
     * has its {@code Properties} id set.
     */
    @SuppressWarnings("unchecked")
    public <R extends T> RegistrySupplier<R> register(ResourceLocation id, Supplier<? extends R> supplier) {
        Entry<T> entry = new Entry<>(id, (Supplier<T>) supplier);
        entries.add(entry);
        if (registered) {
            Registrar<T> registrar = getRegistrar();
            entry.value = registrar.register(entry.id, entry.supplier);
        }
        return (RegistrySupplier<R>) entry;
    }

    /**
     * Registers a supplier that receives the entry {@link ResourceKey}.
     *
     * @param id       the full identifier of the entry
     * @param supplier a factory taking the entry key
     */
    public <R extends T> RegistrySupplier<R> register(ResourceLocation id, Function<ResourceKey<T>, ? extends R> supplier) {
        return register(id, (Supplier<? extends R>) () -> supplier.apply(ResourceKey.create(this.key, id)));
    }

    /**
     * Performs registration of all queued entries.
     */
    public void register() {
        if (registered) {
            throw new IllegalStateException("Cannot register a deferred register twice!");
        }
        registered = true;
        Registrar<T> registrar = getRegistrar();
        for (Entry<T> entry : entries) {
            entry.value = registrar.register(entry.id, entry.supplier);
        }
    }

    @Override
    public @NotNull Iterator<RegistrySupplier<T>> iterator() {
        return entryView.iterator();
    }

    public RegistrarManager getRegistrarManager() {
        return managerSupplier.get();
    }

    public Registrar<T> getRegistrar() {
        return managerSupplier.get().get(key);
    }

    private class Entry<R> implements RegistrySupplier<R> {
        private final ResourceLocation id;
        private final Supplier<R> supplier;
        private RegistrySupplier<R> value;

        Entry(ResourceLocation id, Supplier<R> supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        @Override
        public boolean isPresent() {
            return value != null && value.isPresent();
        }

        @Override
        public R get() {
            if (isPresent()) {
                return value.get();
            }
            throw new NullPointerException("Registry object not present: " + id);
        }

        @Override
        public ResourceLocation getRegistryId() {
            return getRegistrar().key().location();
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        private Registrar<R> getRegistrar() {
            //noinspection unchecked
            return (Registrar<R>) DeferredRegister.this.getRegistrar();
        }
    }
}
