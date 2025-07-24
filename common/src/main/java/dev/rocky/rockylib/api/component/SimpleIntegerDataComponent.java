package dev.rocky.rockylib.api.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
/**
 * A simple record class that represents an integer-based data component.
 *
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public record SimpleIntegerDataComponent(int value) {

    /**
     * A codec for serializing and deserializing SimpleIntegerDataComponent instances.
     * This codec maps the "value" field to an integer during serialization.
     */
    public static final Codec<SimpleIntegerDataComponent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("value") // Maps the
                            // "value" field for encoding/decoding.
                            .forGetter(SimpleIntegerDataComponent::value))
                    .apply(instance, SimpleIntegerDataComponent::new));

    /**
     * Returns a new instance of SimpleIntegerDataComponent with a value of 0.
     *
     * @return A SimpleIntegerDataComponent with a default value of 0.
     */
    public static SimpleIntegerDataComponent empty() {
        return new SimpleIntegerDataComponent(0);
    }

    /**
     * Increments the value by 1 and returns a new instance with the incremented value.
     *
     * @return A new SimpleIntegerDataComponent with the incremented value.
     */
    public SimpleIntegerDataComponent increment() {
        return new SimpleIntegerDataComponent(value + 1);
    }

    /**
     * Decrements the value by 1 and returns a new instance with the decremented value.
     *
     * @return A new SimpleIntegerDataComponent with the decremented value.
     */
    public SimpleIntegerDataComponent decrement() {
        return new SimpleIntegerDataComponent(value - 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof SimpleIntegerDataComponent ex && this.value == ex.value;
        }
    }
}
