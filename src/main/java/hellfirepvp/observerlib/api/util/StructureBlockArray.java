package hellfirepvp.observerlib.api.util;

import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.TickPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is an exemplary simple implementation of the matchable structure interface.
 * Can be used to observe structure integrity with observers.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureBlockArray
 * Created by HellFirePvP
 * Date: 11.08.2019 / 09:12
 */
public class StructureBlockArray extends BlockArray implements MatchableStructure {

    public <T extends LevelWriter & LevelReader> Map<BlockPos, BlockState> place(T levelAccess, BlockPos center) {
        return place(levelAccess, center, pos -> true);
    }

    public <T extends LevelWriter & LevelReader> Map<BlockPos, BlockState> place(T levelAccess, BlockPos center, Predicate<BlockPos> posFilter) {
        Map<BlockPos, BlockState> placedResult = new HashMap<>();
        this.getContents().forEach((offset, matchableState) -> {
            BlockPos at = center.offset(offset);
            if (!posFilter.test(at)) return;

            BlockState state = matchableState.getDescriptiveState(0);
            BlockState existing = levelAccess.getBlockState(at);

            //Copy waterlogged state
            if (!existing.getFluidState().isEmpty() &&
                    existing.getFluidState().is(FluidTags.WATER) &&
                    state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (!levelAccess.setBlock(at, state, Block.UPDATE_ALL)) return;
            placedResult.put(at, state);

            if (levelAccess instanceof Level level) {
                if (!state.getFluidState().isEmpty()) {
                    Fluid fluid = state.getFluidState().getType();
                    level.scheduleTick(at, fluid, fluid.getTickDelay(level), TickPriority.HIGH);
                }
            }

            BlockEntity placedTile = levelAccess.getBlockEntity(at);
            if (placedTile != null && hasTileAt(offset)) {
                MatchableTile matchTile = getTileEntityAt(offset);
                if (matchTile != null) {
                    matchTile.postPlacement(placedTile, levelAccess, offset);
                }
            }
        });
        return placedResult;
    }
}
