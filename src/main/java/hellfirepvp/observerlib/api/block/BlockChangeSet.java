package hellfirepvp.observerlib.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockChangeSet
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:17
 */
public interface BlockChangeSet {

    public void reset();

    public boolean hasChange(BlockPos pos);

    public boolean isEmpty();

    @Nonnull
    public Collection<StateChange> getChanges();

    public static interface StateChange {

        @Nonnull
        public BlockPos getAbsolutePosition();

        @Nonnull
        public BlockPos getRelativePosition();

        @Nonnull
        public IBlockState getOldState();

        @Nonnull
        public IBlockState getNewState();

    }

}
