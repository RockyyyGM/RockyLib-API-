package dev.rocky.rockylib.api.enchantment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public class EnchantmentUtils {
    public static boolean containsEnchantment(ItemStack stack, ResourceLocation enchantment) {
        if (!stack.isEnchanted() || stack.getEnchantments().isEmpty()) {
            return false;
        }

        final boolean[] hasEnchantment = {false};

        stack.getEnchantments().entrySet().forEach(holderEntry -> {
            var isTheEnchantment = holderEntry.getKey().is(enchantment);
            if (isTheEnchantment && !hasEnchantment[0]) {
                hasEnchantment[0] = true;
            }
        });
        return hasEnchantment[0];
    }
}
