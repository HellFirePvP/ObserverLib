package hellfirepvp.observerlib.client.util;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.api.tile.MatchableTile;
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
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
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
public class RenderWorld implements IWorldReader {

    private static final int MAX_LIGHT = 15;

    private final Biome globalBiome;
    private final Dimension thisDim;
    private final WorldBorder maxBorder;

    private final Structure structure;

    public RenderWorld(Structure structure, Biome globalBiome) {
        this.structure = structure;
        this.globalBiome = globalBiome;
        this.thisDim = Minecraft.getInstance().world.getDimension();
        this.maxBorder = this.thisDim.createWorldBorder();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos)) {
            return null;
        }
        MatchableState state = this.structure.getContents().get(pos);
        if (state == null) {
            return null;
        }
        TileEntity tile = state.createTileEntity(this, ClientTickHelper.getClientTick());
        if (tile == null) {
            return null;
        }
        tile.setPos(pos);

        MatchableTile tileMatch = this.structure.getTileEntities().get(pos);
        if (tileMatch == null) {
            return tile;
        }
        CompoundNBT tag = new CompoundNBT();
        tile.write(tag);
        tileMatch.writeDisplayData(tile, ClientTickHelper.getClientTick(), tag);
        tile.read(tag);
        return tile;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos)) {
            return null;
        }
        MatchableState state = this.structure.getContents().get(pos);
        return state == null ? Blocks.AIR.getDefaultState() : state.getDescriptiveState(ClientTickHelper.getClientTick());
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return globalBiome;
    }

    @Override
    public int getLightFor(LightType lightType, BlockPos blockPos) {
        return 0;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
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
