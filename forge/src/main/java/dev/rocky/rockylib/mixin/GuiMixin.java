package dev.rocky.rockylib.mixin;

import dev.rocky.rockylib.RockyLibMod;
import dev.rocky.rockylib.api.event.v1.events.common.client.HudEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    /**
     * This method is called after the HUD is rendered, allowing us to inject custom rendering logic.
     * We check if the current screen is not a ReceivingLevelScreen to avoid conflicts with loading screens.
     * This matches the Fabric code for HudRenderCallback, which is deprecated. I'll change to a more robust
     * solution if Forge ever provides a better way to handle HUD rendering events, or I roll my own.
     *
     * @param guiGraphics   the {@link GuiGraphics} instance used for rendering
     * @param deltaTracker  the {@link DeltaTracker} instance used for tracking deltas
     * @param ci            the callback info
     */
    @Inject(method = "render", at = @org.spongepowered.asm.mixin.injection.At("TAIL"), remap = false)
    public void amber$render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // this check mirrors the vanilla check
        if (this.minecraft.screen == null || !(this.minecraft.screen instanceof ReceivingLevelScreen)) {
            HudEvents.RENDER_HUD.invoker().onHudRender(guiGraphics, deltaTracker);
        }
    }

    static {
        RockyLibMod.ROCKYLIB_MIXINS.add("GuiMixin");
    }
}
