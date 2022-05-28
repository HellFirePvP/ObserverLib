package hellfirepvp.observerlib.api.client;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import hellfirepvp.observerlib.api.util.SingleBiomeManager;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import hellfirepvp.observerlib.common.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This class supports to render a structure, centered around structure origin 0, 0, 0.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderWorld
 * Created by HellFirePvP
 * Date: 30.04.2019 / 22:22
 */
@OnlyIn(Dist.CLIENT)
public class StructureRenderWorld implements LevelReader {

    private static final int MAX_LIGHT = 15;

    private final Holder<Biome> globalBiome;
    private final SingleBiomeManager biomeManager;
    private final DimensionType thisDimType;
    private final WorldBorder maxBorder;

    private final Structure structure;
    private final Stack<Predicate<BlockPos>> blockFilter = new Stack<>();

    public StructureRenderWorld(Structure structure, Holder<Biome> globalBiome) {
        this.structure = structure;
        this.globalBiome = globalBiome;
        this.biomeManager = new SingleBiomeManager(this.globalBiome);
        DimensionType dimType = RegistryUtil.client().getValue(Registry.DIMENSION_TYPE_REGISTRY, DimensionType.OVERWORLD_LOCATION);
        if (dimType == null) {
            dimType = Iterables.getFirst(RegistryUtil.client().getValues(Registry.DIMENSION_TYPE_REGISTRY), null);
        }
        this.thisDimType = dimType;

        if (this.thisDimType.coordinateScale() != 1.0D) {
            this.maxBorder = new WorldBorder() {
                public double getCenterX() {
                    return super.getCenterX() / thisDimType.coordinateScale();
                }

                public double getCenterZ() {
                    return super.getCenterZ() / thisDimType.coordinateScale();
                }
            };
        } else {
            this.maxBorder = new WorldBorder();
        }
    }

    public void pushContentFilter(@Nonnull Predicate<BlockPos> blockFilter) {
        this.blockFilter.push(blockFilter);
    }

    public void popContentFilter() {
        this.blockFilter.pop();
    }

    private boolean allowAccess(BlockPos pos) {
        for (Predicate<BlockPos> filter : this.blockFilter) {
            if (!filter.test(pos)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos) || !allowAccess(pos)) {
            return null;
        }
        MatchableState state = this.structure.getBlockStateAt(pos);
        BlockEntity tile = state.createTileEntity(this, pos, ClientTickHelper.getClientTick());
        if (tile == null) {
            return null;
        }
        tile.setLevel(Minecraft.getInstance().level);

        MatchableTile tileMatch = this.structure.getTileEntityAt(pos);
        if (tileMatch == null) {
            return tile;
        }
        CompoundTag tag = tile.saveWithoutMetadata();
        tileMatch.writeDisplayData(tile, ClientTickHelper.getClientTick(), tag);
        tile.load(tag);

        tileMatch.postPlacement(tile, this, pos);
        return tile;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos) || !allowAccess(pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        MatchableState state = this.structure.getContents().get(pos);
        return state == null ? Blocks.AIR.defaultBlockState() : state.getDescriptiveState(ClientTickHelper.getClientTick());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    //Something with lighting and AO it seems?
    //Some light-multiplier based on direction? seems sketchy tbh
    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShade(Direction direction, boolean b) {
        return 1F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return new StructureRenderLightManager(MAX_LIGHT);
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return MAX_LIGHT;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount) {
        return MAX_LIGHT;
    }

    @Override
    public int getMaxLightLevel() {
        return MAX_LIGHT;
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return null;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return 0;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return biomeManager;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return globalBiome;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return maxBorder;
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity entityIn, VoxelShape shape) {
        return true;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity p_186427_, AABB p_186428_) {
        return Lists.newArrayList();
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public DimensionType dimensionType() {
        return this.thisDimType;
    }
}
