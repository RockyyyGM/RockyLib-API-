package dev.rocky.rockylib.api.common.client;

import dev.rocky.rockylib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Utility class for common client-side operations in the Amber API.
 * This class provides methods that are useful across different client implementations.
 *
 * @since 6.0.6
 */
public class CommonClientUtils {
    public static final int WHITE = 0xFFFFFFFF; // Default white color for text rendering
    public static final int BLACK = 0x000000FF; // Default black color for text rendering
    public static final int TRANSPARENT = 0x00000000; // Transparent color for text rendering
    public static final int PACKED_LIGHT = 15728880;

    // --------------------- HUD ---------------------

    /**
     * Checks HUDs should be rendered based on the current game state.
     * Takes into account whether the debug screen is shown, GUI is hidden, and the player is null.
     *
     * @return true if the HUD should be rendered, false otherwise.
     */
    public static boolean shouldRender() {
        Minecraft mc = Minecraft.getInstance();
        try {
            return !(mc.getDebugOverlay().showDebugScreen() || mc.options.hideGui || mc.level == null || mc.player == null);
        } catch (Exception e) {
            // Handle any exceptions that may occur
            Constants.LOG.error("An error occurred while checking if the HUD should be rendered: {}", e.getMessage());
            return false; // If an error occurs, do not render the HUD
        }
    }

    /**
     * Renders text at the specified coordinates with the given color.
     *
     * @param context The graphics context for rendering the GUI.
     * @param font    The font renderer to use for rendering text.
     * @param message The text component to render.
     * @param x       the x-coordinate where to start writing.
     * @param y       the y-coordinate where to start writing.
     * @param color   the color to use for rendering the text (specify an alpha value, or it will be invisible).
     */
    public static void text(GuiGraphics context, Font font, Component message, int x, int y, int color) {
        context.drawString(font, message, x, y, color);
    }

    /**
     * Renders a tooltip for the given item stack at the specified coordinates.
     *
     * @param guiGraphics The graphics context for rendering the GUI.
     * @param stack       The item stack for which to render the tooltip.
     * @param x           The x-coordinate for rendering the tooltip.
     * @param y           The y-coordinate for rendering the tooltip.
     */
    public static void renderTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        // converts a list of components to a list of ClientTooltipComponents
        List<ClientTooltipComponent> tooltipComponents = Screen.getTooltipFromItem(mc, stack)
                .stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();

        guiGraphics.renderTooltip(
                mc.font,
                tooltipComponents,
                x,
                y,
                DefaultTooltipPositioner.INSTANCE,
                stack.get(DataComponents.TOOLTIP_STYLE)
        );
    }

    /**
     * Helper class for writing text to the screen with a cursor position.
     * Maintains an internal cursor and supports writing at specific positions or sequential lines.
     *
     * <h2>Usage Examples</h2>
     * <pre><code>
     * // Example 1: Start at (20, 40) and write two lines
     * CommonClientUtils.TextWriter writer = new CommonClientUtils.TextWriter(graphics, font, 20, 40);
     * writer.writeLine(Component.literal("First line"), CommonClientUtils.WHITE);
     * writer.writeLine(Component.literal("Second line"));
     *
     * // Example 2: Inline writing and repositioning
     * writer.write(Component.literal("Inline start"));
     * writer.write(Component.literal(" at x=100,y=100"), 100, 100);
     *
     * // Example 3: Using default color for lines
     * writer.writeLine(Component.literal("Default color line"));
     * </code></pre>
     *
     * @since 6.0.6
     */
    public static final class TextWriter {
        private final GuiGraphics context;
        private final Font font;

        private int cursorX = 0; // Current cursor X position
        private int cursorY = 0; // Current cursor Y position

        // TODO: Add line width and anchor to offer automatic wrapping and alignment.
        // See GuiGraphics.drawWordWrap()

        /**
         * Constructs a TextWriter with the specified graphics context and font.
         *
         * @param context the GuiGraphics context to draw text
         * @param font    the Font renderer
         */
        public TextWriter(GuiGraphics context, Font font) {
            this.context = context;
            this.font = font;
        }

        /**
         * Constructs a TextWriter with the specified graphics context, font, and initial cursor position.
         *
         * @param context the GuiGraphics context to draw text
         * @param font    the Font renderer
         * @param x       the initial X coordinate for the cursor
         * @param y       the initial Y coordinate for the cursor
         */
        public TextWriter(GuiGraphics context, Font font, int x, int y) {
            this(context, font);
            this.cursorX = x;
            this.cursorY = y;
        }

        /**
         * Writes the specified message at the given coordinates with the specified color.
         * Updates the internal cursor position to the provided coordinates.
         *
         * @param message the text component to render
         * @param x       the X coordinate where to start writing
         * @param y       the Y coordinate where to start writing
         * @param color   the color to use for rendering the text
         */
        public void write(Component message, int x, int y, int color) {
            cursorX = x;
            cursorY = y;
            text(context, font, message, cursorX, cursorY, color);
        }

        /**
         * Writes the specified message at the given coordinates using the default text color.
         * Updates the internal cursor position to the provided coordinates.
         *
         * @param message the text component to render
         * @param x       the X coordinate where to start writing
         * @param y       the Y coordinate where to start writing
         */
        public void write(Component message, int x, int y) {
            cursorX = x;
            cursorY = y;
            write(message, cursorX, cursorY, WHITE);
        }

        /**
         * Writes the specified message at the current cursor position using the default text color.
         *
         * @param message the text component to render
         */
        public void write(Component message) {
            write(message, cursorX, cursorY, WHITE);
        }

        /**
         * Writes the specified message at the current cursor position with the specified color,
         * then moves the cursor down by one line height.
         *
         * @param message the text component to render
         * @param color   the color to use for rendering the text
         */
        public void writeLine(Component message, int color) {
            text(context, font, message, cursorX, cursorY, color);
            cursorY += font.lineHeight; // Move cursor down by one line height
        }

        /**
         * Writes the specified message at the current cursor position using the default text color,
         * then moves the cursor down by one line height.
         *
         * @param message the text component to render
         */
        public void writeLine(Component message) {
            writeLine(message, WHITE);
        }
    }
}
