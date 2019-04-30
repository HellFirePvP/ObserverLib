package hellfirepvp.observerlib.client.util;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
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
        this.thisDim = DimensionType.OVERWORLD.create();
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
        NBTTagCompound tag = new NBTTagCompound();
        tile.write(tag);
        tileMatch.writeDisplayData(tile, ClientTickHelper.getClientTick(), tag);
        tile.read(tag);
        return tile;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
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
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).isAir(this, pos);
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true; //Maybe needs more attention
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return globalBiome;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return MAX_LIGHT;
    }

    @Override
    public int getLightFor(EnumLightType type, BlockPos pos) {
        return MAX_LIGHT;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return MAX_LIGHT;
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return false;
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return 0;
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return null;
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
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
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
