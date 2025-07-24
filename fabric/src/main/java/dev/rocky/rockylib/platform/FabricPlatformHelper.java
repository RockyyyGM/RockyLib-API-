package dev.rocky.rockylib.platform;

import dev.rocky.rockylib.api.platform.v1.ModInfo;
import dev.rocky.rockylib.platform.services.IPlatformHelper;
import dev.rocky.rockylib.util.Env;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Env getEnvironment() {
        // wat?
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> Env.CLIENT;
            case SERVER -> Env.SERVER;
            default -> throw new IllegalStateException("Unknown environment type: " + FabricLoader.getInstance()
                    .getEnvironmentType());
        };
    }

    @Override
    public Collection<String> getModIds() {
        return FabricLoader.getInstance()
                .getAllMods()
                .stream()
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getId)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable ModInfo getModInfo(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(mod -> {
            ModMetadata metadata = mod.getMetadata();
            return new ModInfo(
                    metadata.getId(),
                    metadata.getName(),
                    metadata.getVersion().getFriendlyString(),
                    metadata.getDescription()
            );
        }).orElse(null);
    }
}
