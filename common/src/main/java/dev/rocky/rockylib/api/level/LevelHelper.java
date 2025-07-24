package dev.rocky.rockylib.api.level;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public class LevelHelper {

    /**
     * Runs a specified function every X ticks within the game world.
     * This is designed to be used in tick() methods.
     *
     * @param level The game level or world instance.
     * @param ticks The number of ticks between each function execution.
     * @param run   The function to execute every X ticks, which takes the current game time as a parameter.
     */
    public static void runEveryXTicks(Level level, int ticks, Consumer<Long> run) {
        // Ensure the level instance is valid.
        if (level == null) return;

        // Get the current game time from the level.
        long gameTime = level.getGameTime();

        // Check if the current game time is evenly divisible by the specified number of ticks.
        // If true, execute the provided function with the game time as an argument.
        if (gameTime % ticks == 0) {
            run.accept(gameTime);
        }
    }

    /**
     * Spawns an item entity in the world at a specified position.
     * This method creates an ItemEntity using the provided ItemStack and places it
     * at the given BlockPos with slight upward motion to simulate a natural drop.
     *
     * @param level The game level or world instance where the item will be dropped.
     * @param stack The ItemStack to spawn as an item entity in the world.
     * @param pos   The position (BlockPos) where the item will be dropped.
     */
    public static void dropItem(Level level, ItemStack stack, Vec3 pos) {
        dropItem(level, stack, pos, new Vec3(0d, 0.2d, 0d));
    }

    public static void dropItem(Level level, ItemLike stack, Vec3 pos) {
        dropItem(level, new ItemStack(stack), pos, new Vec3(0d, 0.2d, 0d));
    }

    /**
     * Spawns an item entity in the world at a specified position.
     * This method creates an ItemEntity using the provided ItemStack and places it
     * at the given BlockPos with the provided motion.
     *
     * @param level The game level or world instance where the item will be dropped.
     * @param stack The ItemStack to spawn as an item entity in the world.
     * @param pos   The position (BlockPos) where the item will be dropped.
     * @param delta The initial velocity for the spawned item.
     */
    public static void dropItem(Level level, ItemStack stack, Vec3 pos, Vec3 delta) {
        if (level == null) return;
        var itemEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), stack, delta.x(), delta.y(), delta.z());
        level.addFreshEntity(itemEntity);
    }

    public static void dropItem(Level level, ItemLike stack, Vec3 pos, Vec3 delta) {
        dropItem(level, new ItemStack(stack), pos, delta);
    }
}
