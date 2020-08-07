package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: PlaceableStructure
 * Created by HellFirePvP
 * Date: 11.08.2019 / 09:16
 */
public interface PlaceableStructure extends Structure {

    default public Map<BlockPos, BlockState> placeInWorld(IWorld world, BlockPos center, Predicate<BlockPos> posFilter) {
        Map<BlockPos, BlockState> result = new HashMap<>();
        for (Map.Entry<BlockPos, ? extends MatchableState> entry : this.getContents().entrySet()) {
            MatchableState match = entry.getValue();
            BlockPos at = center.add(entry.getKey());
            if (!posFilter.test(at)) {
                continue;
            }

            BlockState state = match.getDescriptiveState(0);
            BlockState existing = world.getBlockState(at);
            if (!existing.getFluidState().isEmpty() &&
                    existing.getFluidState().isTagged(FluidTags.WATER) &&
                    state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.with(BlockStateProperties.WATERLOGGED, true);
            }

            if (!world.setBlockState(at, state, Constants.BlockFlags.DEFAULT)) {
                continue;
            }
            result.put(at, state);

            if (!state.getFluidState().isEmpty()) {
                Fluid f = state.getFluidState().getFluid();
                world.getPendingFluidTicks().scheduleTick(at, f, f.getTickRate(world), TickPriority.HIGH);
            }

            TileEntity placed = world.getTileEntity(at);
            if (placed != null && hasTileAt(entry.getKey())) {
                MatchableTile matchTile = getTileEntityAt(entry.getKey());
                if (matchTile != null) {
                    matchTile.postPlacement(placed, world, entry.getKey());
                }
            }
        }
        return result;
    }

    default public Map<BlockPos, BlockState> placeInWorld(IWorld world, BlockPos center, Predicate<BlockPos> posFilter, PastPlaceProcessor processor) {
        Map<BlockPos, BlockState> result = this.placeInWorld(world, center, posFilter);
        if(processor != null) {
            for (Map.Entry<BlockPos, BlockState> entry : result.entrySet()) {
                if (posFilter.test(entry.getKey())) {
                    processor.process(world, entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    public static interface PastPlaceProcessor {

        void process(IWorld world, BlockPos pos, BlockState currentState);

    }

}
