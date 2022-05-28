package hellfirepvp.observerlib.api.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * An interface for blocks to allow for easy removal of {@link hellfirepvp.observerlib.api.ChangeSubscriber}
 * instances when this {@link #removeWithNewState(Level, BlockPos, BlockState, BlockState)} returns true.
 *
 * Warning! If this does not return true, it's up to "you" to determine when the ChangeSubscriber at this
 * position needs to be removed!
 * Calling {@link hellfirepvp.observerlib.api.ObserverHelper#removeObserver(Level, BlockPos)} has the same effect
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
    default boolean removeWithNewState(Level world, BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState != newState;
    }

}
