package hellfirepvp.observerlib.api.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

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
        super(FakeLightChunkGetter.INSTANCE, false, false);
        this.lightLevel = lightLevel;
    }

    @Override
    public void checkBlock(BlockPos pPos) {}

    @Override
    public boolean hasLightWork() {
        return false;
    }

    @Override
    public int runLightUpdates() {
        return 0;
    }

    @Override
    public void updateSectionStatus(BlockPos pPos, boolean pIsQueueEmpty) {}

    @Override
    public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {}

    @Override
    public void setLightEnabled(ChunkPos pChunkPos, boolean pLightEnabled) {}

    @Override
    public void propagateLightSources(ChunkPos pChunkPos) {}

    @Override
    public LayerLightEventListener getLayerListener(LightLayer pType) {
        return new ConstantLightEngine(this.lightLevel);
    }

    @Override
    public String getDebugData(LightLayer pLightLayer, SectionPos pSectionPos) {
        return "n/a";
    }

    @Override
    public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer pLightLayer, SectionPos pSectionPos) {
        return LayerLightSectionStorage.SectionType.EMPTY;
    }

    @Override
    public void queueSectionData(LightLayer pLightLayer, SectionPos pSectionPos, @Nullable DataLayer pDataLayer) {}

    @Override
    public void retainData(ChunkPos pPos, boolean pRetain) {}

    @Override
    public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
        return super.getRawBrightness(pBlockPos, pAmount);
    }

    @Override
    public boolean lightOnInSection(SectionPos pSectionPos) {
        return true;
    }

    //Mainly concerned with network sync for lights? unnecessary for rendering it seems
    @Override
    public int getLightSectionCount() {
        return 0;
    }

    @Override
    public int getMinLightSection() {
        return 0;
    }

    @Override
    public int getMaxLightSection() {
        return 0;
    }

    private static class FakeLightChunkGetter implements LightChunkGetter {

        private static final FakeLightChunkGetter INSTANCE = new FakeLightChunkGetter();

        @Nullable
        @Override
        public LightChunk getChunkForLighting(int pChunkX, int pChunkZ) {
            return null;
        }

        @Override
        public BlockGetter getLevel() {
            return FakeBlockGetter.INSTANCE;
        }
    }

    private static class FakeBlockGetter implements BlockGetter {

        private static final FakeBlockGetter INSTANCE = new FakeBlockGetter();

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pPos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos p_45571_) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pPos) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    }

    private record ConstantLightEngine(int lightLevel) implements LayerLightEventListener {

        @Nullable
        @Override
        public DataLayer getDataLayerData(SectionPos pos) {
            return null;
        }

        @Override
        public int getLightValue(BlockPos pos) {
            return this.lightLevel;
        }

        @Override
        public void checkBlock(BlockPos pos) {}

        @Override
        public boolean hasLightWork() {
            return false;
        }

        @Override
        public int runLightUpdates() {
            return 0;
        }

        @Override
        public void updateSectionStatus(SectionPos sectionPos, boolean isQueueEmpty) {}

        @Override
        public void setLightEnabled(ChunkPos chPos, boolean isLightEnabled) {}

        @Override
        public void propagateLightSources(ChunkPos chPos) {}
    }
}
