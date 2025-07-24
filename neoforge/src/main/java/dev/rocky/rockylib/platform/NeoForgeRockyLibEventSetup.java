package dev.rocky.rockylib.platform;

import dev.rocky.rockylib.Constants;
import dev.rocky.rockylib.RockyLibMod;
import dev.rocky.rockylib.api.event.v1.events.common.CommandEvents;
import dev.rocky.rockylib.api.event.v1.events.common.LootEvents;
import dev.rocky.rockylib.api.event.v1.events.common.PlayerEvents;
import dev.rocky.rockylib.api.event.v1.events.common.client.ClientCommandEvents;
import dev.rocky.rockylib.api.event.v1.events.common.client.HudEvents;
import dev.rocky.rockylib.api.keymapping.KeybindHelper;
import dev.rocky.rockylib.platform.services.IRockyLibEventSetup;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static net.minecraft.world.InteractionResult.CONSUME;
import static net.minecraft.world.InteractionResult.SUCCESS;

public class NeoForgeRockyLibEventSetup implements IRockyLibEventSetup {
    @Override
    public void registerCommon() {
        NeoForge.EVENT_BUS.register(EventHandlerCommonNeoForge.class);
//        IEventBus bus = (IEventBus) AmberMod.getEventBus(Constants.MOD_ID);
//        assert bus != null;
//        bus.register(EventHandlerCommonNeoForge.EventHandlerCommonMod.class);
    }

    @Override
    public void registerClient() {
        NeoForge.EVENT_BUS.register(EventHandlerClientNeoForge.class);
        IEventBus bus = (IEventBus) RockyLibMod.getEventBus(Constants.MOD_ID);
        assert bus != null;
        bus.register(EventHandlerClientMod.class);
    }

    @Override
    public void registerServer() {
//        NeoForge.EVENT_BUS.register(EventHandlerServer.class);
    }

    static public class EventHandlerCommonNeoForge {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void event(LootTableLoadEvent event) {
            LootEvents.MODIFY.invoker().modify(event.getName(), lootPool -> event.getTable().addPool(lootPool.build()));
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void event(PlayerInteractEvent.EntityInteract event) {
            InteractionResult result = PlayerEvents.ENTITY_INTERACT.invoker()
                    .interact(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget());

            LogicalSide side = event.getSide();

            if (result.equals(InteractionResult.PASS)) {
                return;
            }

            // These checks make sure the event handling is equivalent to Fabric's.
            if (side.isClient()) {
                if (result == SUCCESS) {
                    event.setCancellationResult(SUCCESS);
                    event.setCanceled(true);
                } else if (result == CONSUME) {
                    event.setCancellationResult(CONSUME);
                    event.setCanceled(true);
                } else {
                    // If the result is FAIL or any other value, cancel the event
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onCommandRegistration(RegisterCommandsEvent event) {
            CommandEvents.EVENT.invoker()
                    .register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }

        static public class EventHandlerCommonMod {
            // TODO: Implement mod-specific event handling if needed
        }
    }

    static public class EventHandlerClientNeoForge {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onCommandRegistration(RegisterClientCommandsEvent event) {
            ClientCommandEvents.EVENT.invoker().register(event.getDispatcher(), event.getBuildContext());
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void eventRenderGameOverlayEvent(RenderGuiEvent.Post event) {
            HudEvents.RENDER_HUD.invoker().onHudRender(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    static public class EventHandlerClientMod {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onKeyMappingRegistration(RegisterKeyMappingsEvent event) {
            Constants.LOG.info("Registering Amber keybindings for NeoForge...");
            for (var keyMapping : KeybindHelper.getKeybindings()) {
                event.register(keyMapping);
            }
            KeybindHelper.forgeEventAlreadyFired = true;
        }
    }

    static public class EventHandlerServer {

    }
}
