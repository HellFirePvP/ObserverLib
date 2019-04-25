package hellfirepvp.observerlib.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatchableBlockState
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:09
 */
public class MatchableBlockState implements MatchableState {

    private static final int CYCLE_STATES = 20;
    private final List<IBlockState> matchingStates;

    public MatchableBlockState(IBlockState... matchingStates) {
        this(Arrays.asList(matchingStates));
    }

    public MatchableBlockState(List<IBlockState> matchingStates) {
        this.matchingStates = matchingStates;
    }

    @Nonnull
    @Override
    public IBlockState getDescriptiveState(long tick) {
        int cycleState = Math.max(2, CYCLE_STATES / matchingStates.size());
        int part = (int) (tick % (cycleState * matchingStates.size()));
        return this.matchingStates.get(part / cycleState);
    }

    @Override
    public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull IBlockState state) {
        return matchingStates.contains(state);
    }

}
