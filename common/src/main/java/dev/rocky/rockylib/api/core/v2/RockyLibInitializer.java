package dev.rocky.rockylib.api.core.v2;

import dev.rocky.rockylib.RockyLibMod;
import dev.rocky.rockylib.api.platform.v1.Platform;
import org.jetbrains.annotations.Nullable;

public class RockyLibInitializer {
    public static RockyLibModInfo initialize(String id, String name, String version, RockyLibModInfo.RockyLibModSide side,
            @Nullable Object eventBus) {
        if (eventBus == null && (Platform.getPlatformName().equals("Forge") || Platform.getPlatformName()
                .equals("NeoForge"))) {
            throw new IllegalArgumentException("Event bus cannot be null for Forge or NeoForge platforms.");
        }
        RockyLibModInfo modInfo = new RockyLibModInfo(id, name, version, side, eventBus);
        RockyLibMod.ROCKYLIB_MODS.add(modInfo);
        return modInfo;
    }
}
