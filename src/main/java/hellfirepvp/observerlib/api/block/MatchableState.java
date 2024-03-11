package hellfirepvp.observerlib.api.block;

import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.api.client.StructureRenderer;
import hellfirepvp.observerlib.common.block.BlockAirRequirement;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatchableState
 * Created by HellFirePvP
 * Date: 25.04.2019 / 19:30
 */
public interface MatchableState {

    /**
     * An exemplary matcher that accepts air-like blocks and displays nothing in rendering.
     */
    public static final MatchableState AIR = new MatchableState() {
        @Nonnull
        @Override
        public BlockState getDescriptiveState(long tick) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
            return state.isAir();
        }
    };

    /**
     * A default matcher allowing for the display of a blockstate that has to be air.
     *
     * Works in conjunction with {@link StructureRenderer} in case it displays required air blocks.
     */
    public static final MatchableState REQUIRES_AIR = new MatchableState() {
        @Nonnull
        @Override
        public BlockState getDescriptiveState(long tick) {
            if (BlockAirRequirement.displayRequiredAir) {
                return ObserverHelper.blockAirRequirement.get().defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
            return state.isAir();
        }
    };

    /**
     * Get a descriptive blockstate for the current matcher for rendering or related.
     * Return {@link Blocks#AIR}'s default state if nothing should be displayed.
     *
     * Generally a blockstate returned here should also be accepted by {@link #matches(BlockGetter, BlockPos, BlockState)}.
     *
     * @param tick an ongoing client tick to cycle through blocks
     *
     * @return a descriptive state representing matcher
     */
    @Nonnull
    public BlockState getDescriptiveState(long tick);

    /**
     * Create a new tileentity for matching help or rendering
     *
     * @param blockReader a world-accessor to create the tileentity in
     * @param tick an ongoing client tick to cycle through blocks
     *
     * @return the created tileentity for the currently cycle'd blockstate
     */
    @Nullable
    default public BlockEntity createTileEntity(BlockGetter blockReader, BlockPos at, long tick) {
        BlockState state = getDescriptiveState(tick);
        if (state.getBlock() instanceof EntityBlock) {
            return ((EntityBlock) state.getBlock()).newBlockEntity(at, state);
        }
        return null;
    }

    /**
     * Test if this matcher considers the blockstate passed in valid.
     *
     * @param reader the current world the blockstate is being matched in
     * @param absolutePosition the absolute position in the world the state is at.
     *                         May be {@link BlockPos#ZERO} if no position is known
     * @param state the blockstate to test if it's valid for this matcher
     *
     * @return if the blockstate is valid
     */
    public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state);

}
