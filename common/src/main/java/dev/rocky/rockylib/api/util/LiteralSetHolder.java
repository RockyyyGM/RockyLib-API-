package dev.rocky.rockylib.api.util;

import java.util.HashSet;

/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public class LiteralSetHolder<T> {
    private final HashSet<T> set = new HashSet<>();

    public T add(T item) {
        set.add(item);
        return item;
    }

    @Deprecated(forRemoval = true)
    public HashSet<T> get() {
        return getSet();
    }

    public HashSet<T> getSet() {
        return set;
    }

    public void clear() {
        set.clear();
    }
}
