package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
public interface MatchableStructure extends IForgeRegistryEntry<MatchableStructure>, Structure {

    /**
     * Test if the change matches at the given central position
     *
     * @param reader the current world
     * @param center the current center of the change matching at
     *
     * @return true, if the entire change's contents match at the given central position, false otherwise
     */
    default public boolean matches(@Nonnull IBlockReader reader, @Nonnull BlockPos center) {
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
    default public boolean matchesSlice(@Nonnull IBlockReader reader, @Nonnull BlockPos center, int yOffset) {
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

    default public boolean matchesSingleBlock(@Nonnull IBlockReader reader,
                                              @Nonnull BlockPos center,
                                              @Nonnull BlockPos centerOffset) {
        return matchesSingleBlock(reader, center, centerOffset, reader.getBlockState(center.add(centerOffset)));
    }

    default public boolean matchesSingleBlock(@Nullable IBlockReader reader,
                                              @Nonnull BlockPos center,
                                              @Nonnull BlockPos centerOffset,
                                              @Nonnull IBlockState comparing) {
        if (!hasBlockAt(centerOffset)) {
            return false;
        } else {
            MatchableState state = getContents().get(centerOffset);
            return state.matches(reader, center.add(centerOffset), comparing);
        }
    }

    @Override
    default MatchableStructure setRegistryName(ResourceLocation name) {
        return this;
    }

    @Override
    default Class<MatchableStructure> getRegistryType() {
        return MatchableStructure.class;
    }

}
