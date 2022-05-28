package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

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

    default public Map<BlockPos, BlockState> placeInWorld(LevelAccessor world, BlockPos center, Predicate<BlockPos> posFilter) {
        Map<BlockPos, BlockState> result = new HashMap<>();
        for (Map.Entry<BlockPos, ? extends MatchableState> entry : this.getContents().entrySet()) {
            MatchableState match = entry.getValue();
            BlockPos at = center.offset(entry.getKey());
            if (!posFilter.test(at)) {
                continue;
            }

            BlockState state = match.getDescriptiveState(0);
            BlockState existing = world.getBlockState(at);
            if (!existing.getFluidState().isEmpty() &&
                    existing.getFluidState().is(FluidTags.WATER) &&
                    state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (!world.setBlock(at, state, Block.UPDATE_ALL)) {
                continue;
            }
            result.put(at, state);

            if (!state.getFluidState().isEmpty()) {
                Fluid f = state.getFluidState().getType();
                world.scheduleTick(at, f, f.getTickDelay(world), TickPriority.HIGH);
            }

            BlockEntity placed = world.getBlockEntity(at);
            if (placed != null && hasTileAt(entry.getKey())) {
                MatchableTile matchTile = getTileEntityAt(entry.getKey());
                if (matchTile != null) {
                    matchTile.postPlacement(placed, world, entry.getKey());
                }
            }
        }
        return result;
    }

    default public Map<BlockPos, BlockState> placeInWorld(LevelAccessor world, BlockPos center, Predicate<BlockPos> posFilter, PastPlaceProcessor processor) {
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

        void process(LevelAccessor world, BlockPos pos, BlockState currentState);

    }

}
