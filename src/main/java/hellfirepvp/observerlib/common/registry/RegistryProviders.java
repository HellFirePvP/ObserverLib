package hellfirepvp.observerlib.common.registry;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryProviders
 * Created by HellFirePvP
 * Date: 24.04.2019 / 18:33
 */
public class RegistryProviders {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ObserverLib.MODID, "observer_providers");
    private static Supplier<IForgeRegistry<ObserverProvider>> REGISTRY;

    public static void initialize(NewRegistryEvent event) {
        REGISTRY = event.create(new RegistryBuilder<ObserverProvider>()
                .setName(REGISTRY_NAME)
                .setType(ObserverProvider.class));
    }

    @Nullable
    public static ObserverProvider getProvider(ResourceLocation key) {
        return REGISTRY.get().getValue(key);
    }

    @Nonnull
    public static Collection<ObserverProvider> getAll() {
        return REGISTRY.get().getValues();
    }

}
