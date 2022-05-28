package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.registry.RegistryStructures;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverProviderStructure
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:16
 */
public class ObserverProviderStructure extends ObserverProvider {

    public ObserverProviderStructure(ResourceLocation registryName) {
        super(registryName);
    }

    @Nonnull
    @Override
    public ChangeObserver provideObserver() {
        MatchableStructure structure = RegistryStructures.getStructure(this.getRegistryName());
        if (structure == null) {
            throw new IllegalStateException("Tried creating structure observer for unknown structure: " + this.getRegistryName());
        }
        return new ChangeObserverStructure(structure);
    }

}
