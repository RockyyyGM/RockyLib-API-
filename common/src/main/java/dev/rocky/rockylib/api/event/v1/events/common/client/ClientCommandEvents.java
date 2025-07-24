package dev.rocky.rockylib.api.event.v1.events.common.client;

import dev.rocky.rockylib.api.event.v1.Event;
import dev.rocky.rockylib.api.event.v1.EventFactory;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Callback for when client commands are registered to the dispatcher.
 *
 * <p>To register some commands, you would register an event listener and implement the callback.
 *
 * Based on the Fabric implementation.
 *
 * @since 6.0.3
 */
public interface ClientCommandEvents {
    Event<ClientCommandEvents> EVENT = EventFactory.createArrayBacked(ClientCommandEvents.class, (callbacks) -> (dispatcher, registryAccess) -> {
        for (ClientCommandEvents callback : callbacks) {
            callback.register(dispatcher, registryAccess);
        }
    });

    /**
     * Called when registering client commands.
     *
     * @param dispatcher the command dispatcher to register commands to
     * @param registryAccess object exposing access to the game's registries
     *
     * May not work on Fabric.
     */
    @ApiStatus.Experimental
    void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess);
}

