package hellfirepvp.observerlib.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * An interface for blocks to allow for easy removal of {@link hellfirepvp.observerlib.api.ChangeSubscriber}
 * instances when this {@link #removeWithNewState(IWorld, BlockPos, IBlockState, IBlockState)} returns true.
 *
 * Warning! If this does not return true, it's up to "you" to determine when the ChangeSubscriber at this
 * position needs to be removed!
 * Calling {@link hellfirepvp.observerlib.api.ObserverHelper#removeObserver(IWorld, BlockPos)} has the same effect
 * as returning true here, so calling this with the appropriate position will remove the subscriber.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockStructureObserver
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:25
 */
public interface BlockStructureObserver {

    /**
     * Test if this current Block at the given world + position should have its ChangeSubscriber removed
     * if this block is broken.
     *
     * @param world the world this block instance is in
     * @param pos the position this block instance is at
     * @param oldState the previous state, a possible blockstate of 'this' block.
     * @param newState the blockstate to replace this blockstate with, might not be 'this' block.
     *
     * @return true to remove the ChangeSubscriber at this position, if any, false to make it persist.
     */
    default boolean removeWithNewState(IWorld world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState != newState;
    }

}
