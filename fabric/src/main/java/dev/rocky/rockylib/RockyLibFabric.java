package dev.rocky.rockylib;

import dev.rocky.rockylib.api.core.v2.RockyLibInitializer;
import dev.rocky.rockylib.api.core.v2.RockyLibModInfo;
import dev.rocky.rockylib.api.keymapping.KeybindHelper;
import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.api.platform.v1.Platform;
import dev.rocky.rockylib.util.Env;
import dev.rocky.rockylib.util.EnvExecutor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

/**
 * Fabric entry point.
 */
public class RockyLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModInfo info = Platform.getModInfo(Constants.MOD_ID);
        assert info != null;
        RockyLibInitializer.initialize(info.id(), info.name(), info.version(), RockyLibModInfo.RockyLibModSide.COMMON, null);
        RockyLibMod.init();

        registerKeybinds();
    }

    private static void registerKeybinds() {
        EnvExecutor.runInEnv(
                Env.CLIENT, () -> () -> {
                    Constants.LOG.info("Registering RockyLib keybindings for Fabric...");
                    KeybindHelper.getKeybindings().forEach(key -> {
                        try {
                            KeyBindingHelper.registerKeyBinding(key);
                        } catch (IllegalArgumentException e) {
                            Constants.LOG.error("Failed to register keybind: {}", key.getName(), e);
                        }
                    });
                }
        );
    }
}
