package dev.rocky.rockylib.util;

import dev.rocky.rockylib.platform.Services;

import java.util.Optional;
import java.util.function.Supplier;

public final class EnvExecutor {
    private EnvExecutor() {
    }

    public static void runInEnv(Env type, Supplier<Runnable> runnableSupplier) {
        if (Services.PLATFORM.getEnvironment() == type) {
            runnableSupplier.get().run();
        }
    }

    public static <T> Optional<T> getInEnv(Env type, Supplier<Supplier<T>> runnableSupplier) {
        if (Services.PLATFORM.getEnvironment() == type) {
            return Optional.ofNullable(runnableSupplier.get().get());
        }

        return Optional.empty();
    }

    public static <T> T getEnvSpecific(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server) {
        if (Services.PLATFORM.getEnvironment() == Env.CLIENT) {
            return client.get().get();
        } else {
            return server.get().get();
        }
    }
}
