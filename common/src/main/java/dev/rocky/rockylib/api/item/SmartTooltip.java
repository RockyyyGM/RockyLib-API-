package dev.rocky.rockylib.api.item;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A utility class for building smart tooltips that respond to player input.
 * This class allows adding tooltip components conditionally based on player
 * key presses or modifier keys.
 */
/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public class SmartTooltip {
    // List of tooltip components to display.
    private final List<Component> tooltipComponents = new ArrayList<>();

    /**
     * Constructs a SmartTooltip.
     */
    public SmartTooltip() {
    }

    /**
     * Adds a component to the tooltip unconditionally.
     *
     * @param component The component to add.
     * @return The current SmartTooltip instance for method chaining.
     */
    public SmartTooltip add(Component component) {
        tooltipComponents.add(component);
        return this;
    }

    /**
     * Adds a component to the tooltip if the player is holding the Shift key.
     *
     * @param component The component to add if Shift is held down.
     * @return The current SmartTooltip instance for method chaining.
     */
    public SmartTooltip shift(Component component) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(component);
        }
        return this;
    }

    /**
     * Adds a component to the tooltip if a specific keybind is pressed.
     *
     * @param keybind   The key mapping to check.
     * @param component The component to add if the keybind is pressed.
     * @return The current SmartTooltip instance for method chaining.
     */
    public SmartTooltip keybind(KeyMapping keybind, Component component) {
        if (keybind.isDown()) {
            tooltipComponents.add(component);
        }
        return this;
    }

    /**
     * Adds a component to the tooltip if both the Shift key and a specific keybind are pressed.
     *
     * @param keybind   The key mapping to check.
     * @param component The component to add if both the Shift key and the keybind are pressed.
     * @return The current SmartTooltip instance for method chaining.
     */
    public SmartTooltip shiftKeybind(KeyMapping keybind, Component component) {
        if (Screen.hasShiftDown() && keybind.isDown()) {
            tooltipComponents.add(component);
        }
        return this;
    }

    /**
     * Appends all accumulated components in this SmartTooltip to an external tooltip list.
     *
     * @param tooltipAdder A consumer that accepts a Component and adds it to the tooltip list.
     * @see Item#appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer, TooltipFlag)
     */
    public void into(Consumer<Component> tooltipAdder) {
        for (Component component : tooltipComponents) {
            tooltipAdder.accept(component);
        }
    }

    /**
     * @deprecated Use {@link #into(Consumer)} instead. This is only here so mods that use
     * SmartTooltip can still compile.
     */
    @Deprecated
    public void into(List<Component> tooltipAdder) {
        tooltipAdder.addAll(tooltipComponents);
    }
}
