package dev.rocky.rockylib.api.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public class FeedbackHelper {
    public static void message(Player player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void actionBarMessage(Player player, Component component) {
        player.displayClientMessage(component, true);
    }
}
