package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.ChangeObserver;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverProvider
 * Created by HellFirePvP
 * Date: 24.04.2019 / 17:47
 */
public abstract class ObserverProvider implements IForgeRegistryEntry<ObserverProvider> {

    private final ResourceLocation registryName;

    public ObserverProvider(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Nonnull
    public abstract ChangeObserver provideObserver();

    @Override
    public final ObserverProvider setRegistryName(ResourceLocation name) {
        return this;
    }

    @Nullable
    @Override
    public final ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @Override
    public final Class<ObserverProvider> getRegistryType() {
        return ObserverProvider.class;
    }

}
