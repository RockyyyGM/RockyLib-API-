package dev.rocky.rockylib.api.commands.v1;

import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.api.platform.v1.Platform;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SimpleCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> createBaseCommand(String modId) {
        return Commands.literal(modId).executes(commandContext -> {
            ModInfo modInfo = Platform.getModInfo(modId);

            // wat??
            if (modInfo == null) {
                commandContext.getSource().sendFailure(Component.literal("Mod info not found!"));
                return Command.SINGLE_SUCCESS;
            }

            commandContext.getSource()
                    .sendSuccess(
                            () -> Component.literal(modInfo.name()).append(" - Version: " + modInfo.version()),
                            false
                    );
            return Command.SINGLE_SUCCESS;
        });
    }
}
