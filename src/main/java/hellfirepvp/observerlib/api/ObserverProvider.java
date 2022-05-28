package hellfirepvp.observerlib.api;

import net.minecraft.resources.ResourceLocation;
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

    /**
     * Provides a new observer of the current provider.
     *
     * The observer MUST return the same registry name in {@link ChangeObserver#getProviderRegistryName()}
     * as this provider's {@link ObserverProvider#getRegistryName()} for deserialization purposes.
     *
     * @return a new observer
     */
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
