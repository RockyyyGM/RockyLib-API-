package dev.rocky.rockylib;

import dev.rocky.rockylib.api.core.v2.RockyLibInitializer;
import dev.rocky.rockylib.api.core.v2.RockyLibModInfo;
import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.api.platform.v1.Platform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class RockyLibNeoForge {
    public RockyLibNeoForge(IEventBus eventBus) {
        ModInfo info = Platform.getModInfo(Constants.MOD_ID);
        assert info != null;
        RockyLibInitializer.initialize(info.id(), info.name(), info.version(), RockyLibModInfo.RockyLibModSide.COMMON, eventBus);
        RockyLibMod.init();
    }
}