package hellfirepvp.observerlib.api.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureRenderLightManager
 * Created by HellFirePvP
 * Date: 05.06.2020 / 22:20
 */
public class StructureRenderLightManager extends LevelLightEngine {

    private final int lightLevel;

    public StructureRenderLightManager(int lightLevel) {
        super(null, false, false);
        this.lightLevel = lightLevel;
    }

    @Override
    public void checkBlock(BlockPos blockPosIn) {}

    @Override
    public void onBlockEmissionIncrease(BlockPos blockPosIn, int emission) {}

    @Override
    public boolean hasLightWork() {
        return false;
    }

    @Override
    public int runUpdates(int toUpdateCount, boolean updateSkyLight, boolean updateBlockLight) {
        return 0;
    }

    @Override
    public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}

    @Override
    public void enableLightSources(ChunkPos chunkPos, boolean p_215571_2_) {}

    @Override
    public LayerLightEventListener getLayerListener(LightLayer type) {
        return new ConstantLightEngine(this.lightLevel);
    }

    @Override
    public String getDebugData(LightLayer p_215572_1_, SectionPos p_215572_2_) {
        return "n/a";
    }

    @Override
    public void updateSectionStatus(BlockPos p_215567_1_, boolean p_215567_2_) {}

    @Override
    public void retainData(ChunkPos pos, boolean retain) {}

    @Override
    public int getRawBrightness(BlockPos blockPosIn, int amount) {
        return Mth.clamp(this.lightLevel - amount, 0, 15);
    }

    @Override
    public void queueSectionData(LightLayer type, SectionPos pos, @Nullable DataLayer array, boolean p_215574_4_) {}

    private static class ConstantLightEngine implements LayerLightEventListener {

        private final int lightLevel;

        public ConstantLightEngine(int lightLevel) {
            this.lightLevel = lightLevel;
        }

        @Nullable
        @Override
        public DataLayer getDataLayerData(SectionPos p_215612_1_) {
            return null;
        }

        @Override
        public int getLightValue(BlockPos worldPos) {
            return lightLevel;
        }

        @Override
        public void checkBlock(BlockPos at) {}

        @Override
        public void onBlockEmissionIncrease(BlockPos pos, int p_164456_) {}

        @Override
        public boolean hasLightWork() {
            return false;
        }

        @Override
        public int runUpdates(int p_164449_, boolean p_164450_, boolean p_164451_) {
            return 0;
        }

        @Override
        public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}

        @Override
        public void enableLightSources(ChunkPos p_164452_, boolean p_164453_) {

        }
    }
}
