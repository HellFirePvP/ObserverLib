package hellfirepvp.observerlib.common.util;

import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public class DistUtil {

    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return switch (FMLEnvironment.dist) {
            case CLIENT -> clientTarget.get().get();
            case DEDICATED_SERVER -> serverTarget.get().get();
        };
    }

}
