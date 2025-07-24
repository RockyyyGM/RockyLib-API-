package dev.rocky.rockylib.api.event.v1.events.common;

import dev.rocky.rockylib.api.event.v1.Event;
import dev.rocky.rockylib.api.event.v1.EventFactory;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Callback for when a server registers all commands.
 *
 * <p>To register some commands, you would register an event listener and implement the callback.
 *
 * <pre>{@code
 * CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
 *     // For example, this command is only registered on an integrated server like the vanilla publish command
 *     if (environment.integrated) dispatcher.register(Commands.literal("integrated_command").executes(context -> {...}));
 * })};
 * }</pre>
 *
 * Based on the Fabric implementation.
 *
 * @since 6.0.3
 */
public interface CommandEvents {
    Event<CommandEvents> EVENT = EventFactory.createArrayBacked(CommandEvents.class, (callbacks) -> (dispatcher, registryAccess, environment) -> {
        for (CommandEvents callback : callbacks) {
            callback.register(dispatcher, registryAccess, environment);
        }
    });

    /**
     * Called when the server is registering commands.
     *
     * @param dispatcher the command dispatcher to register commands to
     * @param registryAccess object exposing access to the game's registries
     * @param environment environment the registrations should be done for, used for commands that are dedicated or integrated server only
     */
    void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment);
}
