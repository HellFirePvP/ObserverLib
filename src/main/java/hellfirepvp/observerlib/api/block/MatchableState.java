package hellfirepvp.observerlib.api.block;

import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.api.client.StructureRenderer;
import hellfirepvp.observerlib.common.CommonProxy;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

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
    public static final MatchableState IS_AIR = new MatchableState() {
        @Nonnull
        @Override
        public BlockState getDescriptiveState(long tick) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
            return state.getMaterial() == Material.AIR;
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
            if (StructureRenderer.displayRequiredAir) {
                return ObserverHelper.blockAirRequirement.getDefaultState();
            }
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
            return state.isAir(reader, absolutePosition);
        }
    };

    /**
     * Get a descriptive blockstate for the current matcher for rendering or related.
     * Return {@link net.minecraft.block.Blocks#AIR}'s default state if nothing should be displayed.
     *
     * Generally a blockstate returned here should also be accepted by {@link #matches(IBlockReader, BlockPos, BlockState)}.
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
    default public TileEntity createTileEntity(IBlockReader blockReader, long tick) {
        return getDescriptiveState(tick).createTileEntity(blockReader);
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
    public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state);

}
