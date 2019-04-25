package hellfirepvp.observerlib.common.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockModifyEvent
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:03
 */
public class BlockModifyEvent extends Event {

    private final IChunk chunk;
    private final IWorld world;
    private final BlockPos at;
    private final IBlockState oldState, newState;

    public BlockModifyEvent(IWorld world, IChunk chunk, BlockPos at, IBlockState oldState, IBlockState newState) {
        this.at = at;
        this.chunk = chunk;
        this.world = world;
        this.oldState = oldState;
        this.newState = newState;
    }

    public BlockPos getPos() {
        return at;
    }

    public IWorld getWorld() {
        return world;
    }

    public IChunk getChunk() {
        return chunk;
    }

    @Nullable
    public TileEntity getNewTileEntity() {
        return world.getTileEntity(getPos());
    }

    public IBlockState getOldState() {
        return oldState;
    }

    public IBlockState getNewState() {
        return newState;
    }

    public Block getOldBlock() {
        return oldState.getBlock();
    }

    public Block getNewBlock() {
        return newState.getBlock();
    }

}
