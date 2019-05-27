package hellfirepvp.observerlib.api.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        public IBlockState getDescriptiveState(long tick) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull IBlockState state) {
            return state.getMaterial() == Material.AIR;
        }
    };

    /**
     * Get a descriptive blockstate for the current matcher for rendering or related.
     * Return {@link net.minecraft.init.Blocks#AIR}'s default state if nothing should be displayed.
     *
     * Generally a blockstate returned here should also be accepted by {@link #matches(IBlockReader, BlockPos, IBlockState)}.
     *
     * @param tick an ongoing client tick to cycle through blocks
     *
     * @return a descriptive state representing matcher
     */
    @Nonnull
    public IBlockState getDescriptiveState(long tick);

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
     *                         May be {@link BlockPos#ORIGIN} if no position is known
     * @param state the blockstate to test if it's valid for this matcher
     *
     * @return if the blockstate is valid
     */
    public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull IBlockState state);

}
