package dev.rocky.rockylib.api.event.v1.events.common;

import dev.rocky.rockylib.api.event.v1.Event;
import dev.rocky.rockylib.api.event.v1.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerEvents {

    /**
     * An event that is fired when a player attempts to interact with (e.g. right-clicks) an entity.
     * <p>
     * Equivalent to Fabric's {@code EntityInteractEvent}.
     * <p>
     *
     * <p>
     * It is invoked from {@link Player#interactOn(Entity, InteractionHand)} before the target
     * entity’s own interaction logic is executed.
     * </p>
     *
     * <p>On the logical server, the return values have the following meaning:</p>
     *  <ul>
     *      <li>PASS falls back to further processing.</li>
     *      <li>Any other value cancels further processing.</li>
     *  </ul>
     *
     * If every listener returns {@code PASS}, vanilla behaviour proceeds unchanged.
     */
    public static final Event<EntityInteract> ENTITY_INTERACT = EventFactory.createArrayBacked(
            EntityInteract.class, callbacks -> (player, world, hand, entity) -> {
                for (EntityInteract callback : callbacks) {
                    InteractionResult result = callback.interact(player, world, hand, entity);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );

    /**
     * Functional interface for handling {@link #ENTITY_INTERACT} callbacks.
     */
    @FunctionalInterface
    public interface EntityInteract {

        /**
         * Called when a player interacts with an entity.
         *
         * @param player the player performing the interaction
         * @param level  the level in which the interaction occurs
         * @param hand   the hand used ({@code MAIN_HAND} or {@code OFF_HAND})
         * @param entity the target entity that was clicked
         * @return an {@link InteractionResult} indicating how the interaction should be handled
         */
        InteractionResult interact(Player player, Level level, InteractionHand hand, Entity entity);
    }
}
