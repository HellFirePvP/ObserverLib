package hellfirepvp.observerlib.api.client;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import hellfirepvp.observerlib.api.util.SingleBiomeManager;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;
import java.util.function.Predicate;

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
public class StructureRenderWorld implements IWorldReader {

    private static final int MAX_LIGHT = 15;

    private final Biome globalBiome;
    private final SingleBiomeManager biomeManager;
    private final Dimension thisDim;
    private final WorldBorder maxBorder;

    private final Structure structure;
    private Stack<Predicate<BlockPos>> blockFilter = new Stack<>();

    public StructureRenderWorld(Structure structure, Biome globalBiome) {
        this.structure = structure;
        this.globalBiome = globalBiome;
        this.biomeManager = new SingleBiomeManager(this.globalBiome);
        this.thisDim = Minecraft.getInstance().world.getDimension();
        this.maxBorder = this.thisDim.createWorldBorder();
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
    public TileEntity getTileEntity(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos) || !allowAccess(pos)) {
            return null;
        }
        MatchableState state = this.structure.getBlockStateAt(pos);
        TileEntity tile = state.createTileEntity(this, ClientTickHelper.getClientTick());
        if (tile == null) {
            return null;
        }
        tile.setWorldAndPos(Minecraft.getInstance().world, pos);

        MatchableTile tileMatch = this.structure.getTileEntityAt(pos);
        if (tileMatch == null) {
            return tile;
        }
        CompoundNBT tag = new CompoundNBT();
        tile.write(tag);
        tileMatch.writeDisplayData(tile, ClientTickHelper.getClientTick(), tag);
        tile.read(tag);

        tileMatch.postPlacement(tile, this, pos);
        return tile;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos) || !allowAccess(pos)) {
            return Blocks.AIR.getDefaultState();
        }
        MatchableState state = this.structure.getContents().get(pos);
        return state == null ? Blocks.AIR.getDefaultState() : state.getDescriptiveState(ClientTickHelper.getClientTick());
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public WorldLightManager getLightManager() {
        return new StructureRenderLightManager(MAX_LIGHT);
    }

    @Override
    public int getLightFor(LightType lightType, BlockPos blockPos) {
        return MAX_LIGHT;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return MAX_LIGHT;
    }

    @Override
    public int getMaxLightLevel() {
        return MAX_LIGHT;
    }

    @Nullable
    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return null;
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return 0;
    }

    @Override
    public int getSkylightSubtracted() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return biomeManager;
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return globalBiome;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return maxBorder;
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return true;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public Dimension getDimension() {
        return thisDim;
    }
}
