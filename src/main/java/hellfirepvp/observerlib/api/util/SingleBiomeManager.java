package hellfirepvp.observerlib.api.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

import net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: SingleBiomeManager
 * Created by HellFirePvP
 * Date: 04.06.2020 / 21:19
 */
public class SingleBiomeManager extends BiomeManager {

    public SingleBiomeManager(Holder<Biome> globalBiome) {
        super((x, y, z) -> globalBiome, 0);
    }

    @Override
    public BiomeManager withDifferentSource(NoiseBiomeSource p_186688_) {
        return this;
    }
}
