package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatchableStructure
 * Created by HellFirePvP
 * Date: 25.04.2019 / 19:22
 */
public interface MatchableStructure extends Structure {

    /**
     * Test if the change matches at the given central position
     *
     * @param reader the current world
     * @param center the current center of the change matching at
     *
     * @return true, if the entire change's contents match at the given central position, false otherwise
     */
    default public boolean matches(@Nonnull BlockGetter reader, @Nonnull BlockPos center) {
        for (Map.Entry<BlockPos, ? extends MatchableState> entry : getContents().entrySet()) {
            if (!matchesSingleBlock(reader, center, entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if all blockstates on this y-offset in relation to the center do match the change.
     *
     * @param reader the current world matching in
     * @param center the current center of the change matching at
     * @param yOffset the y-offset in relation to the center this slice to be matched
     *
     * @return true, if all blockstates on that y-offset do match, false otherwise
     */
    default public boolean matchesSlice(@Nonnull BlockGetter reader, @Nonnull BlockPos center, int yOffset) {
        if (getMinimumOffset().getY() > yOffset || getMaximumOffset().getY() < yOffset) {
            return true;
        }

        for (BlockPos pos : getContents().keySet().stream()
                .filter(pos -> pos.getY() == yOffset)
                .collect(Collectors.toList())) {

            if (!matchesSingleBlock(reader, center, pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if a single position in this world matches the structure's expected blockstate at that position.
     *
     * @param reader the current world matching in
     * @param center the current center of the change matching at
     * @param centerOffset the offset from the center to check the matching at
     *
     * @return true, if the blockstate at the offset of the center does match the structure's expectations, false if not
     */
    default public boolean matchesSingleBlock(@Nonnull BlockGetter reader,
                                              @Nonnull BlockPos center,
                                              @Nonnull BlockPos centerOffset) {
        return matchesSingleBlock(reader, center, centerOffset,
                reader.getBlockState(center.offset(centerOffset)),
                reader.getBlockEntity(center.offset(centerOffset)));
    }

    /**
     * Test if the blockstate given matches the structure's matcher at the given center-offset.
     *
     * @param reader the current world matching in
     * @param center the current center of the change matching at
     * @param centerOffset the offset from the center to check the matching at
     * @param comparing the blockstate to check for validity
     *
     * @return true, if the blockstate at the offset of the center does match the structure's expectations, false if not
     */
    default public boolean matchesSingleBlock(@Nullable BlockGetter reader,
                                              @Nonnull BlockPos center,
                                              @Nonnull BlockPos centerOffset,
                                              @Nonnull BlockState comparing,
                                              @Nullable BlockEntity tileEntity) {
        if (!hasBlockAt(centerOffset)) {
            return false;
        } else {
            MatchableState state = getBlockStateAt(centerOffset);
            MatchableTile tileMatch = getTileEntityAt(centerOffset);
            return state.matches(reader, center.offset(centerOffset), comparing) &&
                    (tileEntity == null || (tileMatch == null || tileMatch.matches(reader, center.offset(centerOffset), tileEntity)));
        }
    }

    public ResourceLocation getRegistryName();
}
