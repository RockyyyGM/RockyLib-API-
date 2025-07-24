package dev.rocky.rockylib;

import dev.rocky.rockylib.api.core.v2.RockyLibInitializer;
import dev.rocky.rockylib.api.core.v2.RockyLibModInfo;
import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.api.platform.v1.Platform;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class RockyLibForge {

    public RockyLibForge(FMLJavaModLoadingContext ctx) {
        ModInfo info = Platform.getModInfo(Constants.MOD_ID);
        assert info != null;
        RockyLibInitializer.initialize(
                info.id(),
                info.name(),
                info.version(),
                RockyLibModInfo.RockyLibModSide.COMMON,
                ctx.getModBusGroup()
        );
        RockyLibMod.init();
    }
}
