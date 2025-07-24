package dev.rocky.rockylib.platform;

import dev.rocky.rockylib.Constants;
import dev.rocky.rockylib.platform.services.IRockyLibEventSetup;
import dev.rocky.rockylib.platform.services.IPlatformHelper;
import dev.rocky.rockylib.platform.services.IRegistrarManager;

import java.util.ServiceLoader;

// Service loaders are a built-in Java feature that allow us to locate implementations of an interface that vary from one
// environment to another. In the context of MultiLoader we use this feature to access a mock API in the common code that
// is swapped out for the platform specific implementation at runtime.
public class Services {

    // In this example we provide a platform helper which provides information about what platform the mod is running on.
    // For example this can be used to check if the code is running on Forge vs Fabric, or to ask the modloader if another
    // mod is loaded.
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    // This is the service that provides the event setup methods for the Amber mod. It allows us to register common,
    // client, and server event handlers in a platform-agnostic way. The actual implementation will vary depending on the
    // platform (Forge, Fabric, etc.) but the interface remains the same.
    public static final IRockyLibEventSetup ROCKYLIB_EVENT_SETUP = load(IRockyLibEventSetup.class);

    // Platform specific registrar manager implementation
    public static final IRegistrarManager REGISTRAR_MANAGER = load(IRegistrarManager.class);

    // This code is used to load a service for the current environment. Your implementation of the service must be defined
    // manually by including a text file in META-INF/services named with the fully qualified class name of the service.
    // Inside the file you should write the fully qualified class name of the implementation to load for the platform. For
    // example our file on Forge points to ForgePlatformHelper while Fabric points to FabricPlatformHelper.
    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}