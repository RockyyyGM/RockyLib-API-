package dev.rocky.rockylib.platform;

import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.platform.services.IPlatformHelper;
import dev.rocky.rockylib.util.Env;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Env getEnvironment() {
        switch (FMLLoader.getDist()) {
            case CLIENT -> {
                return Env.CLIENT;
            }
            case DEDICATED_SERVER -> {
                return Env.SERVER;
            }
            default -> {
                throw new IllegalStateException("Unknown environment type: " + FMLLoader.getDist());
            }
        }
    }

    @Override
    public Collection<String> getModIds() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).collect(Collectors.toList());
    }

    @Override
    public @Nullable dev.rocky.rockylib.api.platform.v1.ModInfo getModInfo(String modId) {
        Optional<ModContainer> maybeContainer =
                ModList.get().getLoadedMods().stream().filter(c -> c.getModId().equals(modId)).findFirst();
        if (maybeContainer.isPresent()) {
            ModContainer container = maybeContainer.get();
            return new ModInfo(
                    container.getModId(),
                    container.getModInfo().getDisplayName(),
                    container.getModInfo().getVersion().toString(),
                    container.getModInfo().getDescription()
            );
        }
        return null;
    }
}