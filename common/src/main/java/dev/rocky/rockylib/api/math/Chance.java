package dev.rocky.rockylib.api.math;

import java.util.Random;

/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public class Chance {
    /**
     * Simulates a random event occurring with a specified probability.
     * <p>
     * This method evaluates a given probability and returns {@code true} if the event
     * is determined to happen based on that probability, or {@code false} if it is not.
     * The probability is defined as a percentage from 0 (0%) to 1 (100%). If the input
     * is below 0 or above 1, it is clamped to fit within this range.
     * </p>
     *
     * @param percent A float value representing the probability (from 0 to 1) that the event will occur.
     *                Values outside this range are clamped to fit within it.
     * @return {@code true} if the event occurs based on the specified probability, {@code false} otherwise.
     */
    public static boolean of(float percent) {
        float chance = clamped(percent);
        if (chance == 0f) {
            return false;
        }
        if (chance == 1f) {
            return true;
        }
        return new Random().nextFloat() > chance;
    }

    /**
     * Clamps a probability value between 0 and 1, ensuring it falls within valid bounds.
     * <p>
     * The input is adjusted to remain between 0 and 1 inclusive. This helps to avoid
     * unexpected behavior due to invalid values being passed.
     * </p>
     *
     * @param percent The raw probability input.
     * @return A clamped probability value from 0 to 1.
     */
    private static float clamped(float percent) {
        return Math.abs(Math.clamp(percent, 0, 1) - 1);
    }
}
