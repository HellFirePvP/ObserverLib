package hellfirepvp.observerlib.api.util;

import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * This class is an exemplary simple implementation of the matchable structure interface.
 * Can be used to observe structure integrity with observers.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: PatternBlockArray
 * Created by HellFirePvP
 * Date: 11.08.2019 / 09:12
 */
public class PatternBlockArray extends BlockArray implements MatchableStructure {

    private final ResourceLocation registryName;

    public PatternBlockArray(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
