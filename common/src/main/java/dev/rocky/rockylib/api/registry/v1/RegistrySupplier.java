package dev.rocky.rockylib.api.registry.v1;

import java.util.function.Consumer;

/**
 * Supplier that also exposes registry information.
 */
public interface RegistrySupplier<T> extends DeferredSupplier<T> {
    /**
     * Runs the given callback once the value is present.
     */
    default void listen(Consumer<T> action) {
        if (isPresent()) {
            action.accept(get());
        }
    }
}
