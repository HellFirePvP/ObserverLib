package hellfirepvp.observerlib.api.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A BlockState change set.
 * Captures BlockState changes for later processing.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockChangeSet
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:17
 */
public interface BlockChangeSet {

    /**
     * Test if the ChangeSet has a change at the given relative position.
     *
     * If true, there will be one StateChange object returned in {@link #getChanges()} which
     * returns a position from {@link StateChange#getRelativePosition()} that is equal to the passed position.
     *
     * @param pos the position to check
     *
     * @return true, if there is a StateChange at this position, false if not.
     */
    public boolean hasChange(BlockPos pos);

    /**
     * Test if the current ChangeSet has any StateChange.
     * Equal to testing {@link #getChanges()} being empty.
     *
     * @return true, if there are no changes in the ChangeSet currently, false if not
     */
    public boolean isEmpty();

    /**
     * Get the current changes in the ChangeSet.
     * The returned collection is immutable.
     *
     * @return the current changes. May be empty.
     */
    @Nonnull
    public Collection<StateChange> getChanges();

    /**
     * Represents a single BlockState change.
     *
     * Contains information about the absolute position of the change in the world,
     * as well as a relative position to the {@link hellfirepvp.observerlib.api.ChangeObserver}'s center
     * this ChangeSet belongs to.
     */
    public static interface StateChange {

        /**
         * Returns the absolute position of the blockstate change in the world.
         *
         * @return the absolute position
         */
        @Nonnull
        public BlockPos getAbsolutePosition();

        /**
         * Returns the relative position of the blockstate change in the world, relative to
         * the {@link hellfirepvp.observerlib.api.ChangeObserver}'s center this ChangeSet belongs to.
         *
         * @return the relative position
         */
        @Nonnull
        public BlockPos getRelativePosition();

        /**
         * Returns the old blockstate that was present before this change occurred.
         * This blockstate is no longer at the absolute position in the world.
         *
         * @return the old blockstate
         */
        @Nonnull
        public BlockState getOldState();

        /**
         * Returns the new blockstate that is currently present in the world at the absolute position.
         *
         * @return the new blockstate
         */
        @Nonnull
        public BlockState getNewState();

    }

}
