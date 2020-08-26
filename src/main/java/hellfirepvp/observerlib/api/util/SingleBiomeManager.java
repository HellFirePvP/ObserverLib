package hellfirepvp.observerlib.api.util;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: SingleBiomeManager
 * Created by HellFirePvP
 * Date: 04.06.2020 / 21:19
 */
public class SingleBiomeManager extends BiomeManager {

    public SingleBiomeManager(Biome globalBiome) {
        super((x, y, z) -> globalBiome, 0, (l, i, i1, i2, iBiomeReader) -> globalBiome);
    }

    @Override
    public BiomeManager copyWithProvider(BiomeProvider newProvider) {
        return this;
    }
}
