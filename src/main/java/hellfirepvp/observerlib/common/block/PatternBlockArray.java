package hellfirepvp.observerlib.common.block;

import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: PatternBlockArray
 * Created by HellFirePvP
 * Date: 25.04.2019 / 22:33
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
