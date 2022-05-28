package hellfirepvp.observerlib.api.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Exemplary implementation of {@link MatchableState} containing a set of passable {@link BlockState}s.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleMatchableBlockState
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:09
 */
public class SimpleMatchableBlockState implements MatchableState {

    private static final int CYCLE_STATES = 20;
    private final List<BlockState> matchingStates;

    public SimpleMatchableBlockState(BlockState... matchingStates) {
        this(Arrays.asList(matchingStates));
    }

    public SimpleMatchableBlockState(List<BlockState> matchingStates) {
        this.matchingStates = matchingStates;
    }

    @Nonnull
    @Override
    public BlockState getDescriptiveState(long tick) {
        int cycleState = Math.max(2, CYCLE_STATES / this.matchingStates.size());
        int part = (int) (tick % (cycleState * this.matchingStates.size()));
        return this.matchingStates.get(part / cycleState);
    }

    @Override
    public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
        return this.matchingStates.contains(state);
    }

}
