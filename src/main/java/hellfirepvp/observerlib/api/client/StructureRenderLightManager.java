package hellfirepvp.observerlib.api.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.lighting.IWorldLightListener;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureRenderLightManager
 * Created by HellFirePvP
 * Date: 05.06.2020 / 22:20
 */
public class StructureRenderLightManager extends WorldLightManager {

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
    public int tick(int toUpdateCount, boolean updateSkyLight, boolean updateBlockLight) {
        return 0;
    }

    @Override
    public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}

    @Override
    public void enableLightSources(ChunkPos chunkPos, boolean p_215571_2_) {}

    @Override
    public IWorldLightListener getLightEngine(LightType type) {
        return new ConstantLightEngine(this.lightLevel);
    }

    @Override
    public String getDebugInfo(LightType p_215572_1_, SectionPos p_215572_2_) {
        return "n/a";
    }

    @Override
    public void func_215567_a(BlockPos p_215567_1_, boolean p_215567_2_) {}

    @Override
    public void retainData(ChunkPos pos, boolean retain) {}

    @Override
    public int getLightSubtracted(BlockPos blockPosIn, int amount) {
        return MathHelper.clamp(this.lightLevel - amount, 0, 15);
    }

    @Override
    public void setData(LightType type, SectionPos pos, @Nullable NibbleArray array, boolean p_215574_4_) {}

    private static class ConstantLightEngine implements IWorldLightListener {

        private final int lightLevel;

        public ConstantLightEngine(int lightLevel) {
            this.lightLevel = lightLevel;
        }

        @Nullable
        @Override
        public NibbleArray getData(SectionPos p_215612_1_) {
            return null;
        }

        @Override
        public int getLightFor(BlockPos worldPos) {
            return lightLevel;
        }

        @Override
        public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}
    }
}
