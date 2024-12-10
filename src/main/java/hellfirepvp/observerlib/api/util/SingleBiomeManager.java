package hellfirepvp.observerlib.api.util;

import net.minecraft.core.BlockPos;
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

    private final Holder<Biome> globalBiome;

    public SingleBiomeManager(Holder<Biome> globalBiome) {
        super((x, y, z) -> globalBiome, 0);
        this.globalBiome = globalBiome;
    }

    @Override
    public BiomeManager withDifferentSource(NoiseBiomeSource src) {
        return this;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        return this.globalBiome;
    }

    @Override
    public Holder<Biome> getNoiseBiomeAtPosition(double pX, double pY, double pZ) {
        return this.globalBiome;
    }

    @Override
    public Holder<Biome> getNoiseBiomeAtPosition(BlockPos pos) {
        return this.globalBiome;
    }

    @Override
    public Holder<Biome> getNoiseBiomeAtQuart(int pX, int pY, int pZ) {
        return this.globalBiome;
    }
}
