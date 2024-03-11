package hellfirepvp.observerlib.common.registry;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ObserverProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryProviders
 * Created by HellFirePvP
 * Date: 24.04.2019 / 18:33
 */
public class RegistryProviders {

    public static final ResourceKey<Registry<ObserverProvider>> REGISTRY_KEY = ResourceKey.createRegistryKey(ObserverLib.key("observer_providers"));
    private static Registry<ObserverProvider> REGISTRY;

    public static void initialize(NewRegistryEvent event) {
        REGISTRY = event.create(new RegistryBuilder<>(REGISTRY_KEY));
    }

    @Nullable
    public static ObserverProvider getProvider(ResourceLocation key) {
        return REGISTRY.get(key);
    }

    @Nonnull
    public static Registry<ObserverProvider> getRegistry() {
        return REGISTRY;
    }
}
