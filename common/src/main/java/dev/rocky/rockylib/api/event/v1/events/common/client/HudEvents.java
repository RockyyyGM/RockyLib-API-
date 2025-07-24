package dev.rocky.rockylib.api.event.v1.events.common.client;

import dev.rocky.rockylib.api.event.v1.Event;
import dev.rocky.rockylib.api.event.v1.EventFactory;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.ApiStatus;

public class HudEvents {
    /* * This event is called after rendering the whole hud, which is displayed in game, in a world.
     * It allows you to render custom elements on the HUD.
     * <p>
     * Note: This event is not called during the loading screen.
     *
     * It uses experimental APIs that might break on the whims of the loader developers. Here be dragons.
     */
    @ApiStatus.Experimental
    public static final Event<RenderHud> RENDER_HUD = EventFactory.createArrayBacked(
            RenderHud.class, (listeners) -> (context, tickCounter) -> {
                for (RenderHud event : listeners) {
                    event.onHudRender(context, tickCounter);
                }
            }
    );

    @FunctionalInterface
    public interface RenderHud {
        /**
         * Called after rendering the whole hud, which is displayed in game, in a world.
         *
         * @param guiGraphics the {@link GuiGraphics} instance
         * @param tickCounter the {@link DeltaTracker} instance
         */
        void onHudRender(GuiGraphics guiGraphics, DeltaTracker tickCounter);
    }
}
