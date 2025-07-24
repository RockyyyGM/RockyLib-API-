package dev.rocky.rockylib.platform;

import dev.rocky.rockylib.api.event.v1.events.common.CommandEvents;
import dev.rocky.rockylib.api.event.v1.events.common.LootEvents;
import dev.rocky.rockylib.api.event.v1.events.common.PlayerEvents;
import dev.rocky.rockylib.api.event.v1.events.common.client.ClientCommandEvents;
import dev.rocky.rockylib.api.event.v1.events.common.client.HudEvents;
import dev.rocky.rockylib.platform.services.IRockyLibEventSetup;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.commands.CommandSourceStack;

public class FabricRockyLibEventSetup implements IRockyLibEventSetup {
    @Override
    public void registerCommon() {
        LootTableEvents.MODIFY.register((resourceKey, builder, lootTableSource, provider) -> {
            LootEvents.MODIFY.invoker().modify(resourceKey.location(), builder::withPool);
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            return PlayerEvents.ENTITY_INTERACT.invoker().interact(player, level, hand, entity);
        });
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            CommandEvents.EVENT.invoker().register(commandDispatcher, commandBuildContext, commandSelection);
        });
    }

    @Override
    public void registerClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            CommandDispatcher<CommandSourceStack> commandsTemp = new CommandDispatcher<>();
            ClientCommandEvents.EVENT.invoker().register(commandsTemp, registryAccess);
        });
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            HudEvents.RENDER_HUD.invoker().onHudRender(guiGraphics, tickDelta);
        });
    }

    @Override
    public void registerServer() {

    }
}
