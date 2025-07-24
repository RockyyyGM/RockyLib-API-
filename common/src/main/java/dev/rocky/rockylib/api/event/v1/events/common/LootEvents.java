package dev.rocky.rockylib.api.event.v1.events.common;

import dev.rocky.rockylib.api.event.v1.Event;
import dev.rocky.rockylib.api.event.v1.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.function.Consumer;

public class LootEvents {
    /**
     * An event that is called to modify a loot table before it is loaded.
     */
    public static final Event<ModifyLootTable> MODIFY = EventFactory.createArrayBacked(
            ModifyLootTable.class, callbacks -> (ResourceLocation lootTable, Consumer<LootPool.Builder> add) -> {
                for (ModifyLootTable callback : callbacks) {
                    callback.modify(lootTable, add);
                }
            }
    );

    @FunctionalInterface
    public interface ModifyLootTable {
        void modify(ResourceLocation lootTable, Consumer<LootPool.Builder> add);
    }
}
