package hellfirepvp.observerlib.api.util;

import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * General utility around structure analysis
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureUtil
 * Created by HellFirePvP
 * Date: 12.02.2020 / 21:21
 */
public class StructureUtil {

    private StructureUtil() {}

    /**
     * Test if all chunks the structure is in are currently loaded.
     *
     * @param structure the structure to test
     * @param world the world to test in
     * @param offset the offset to test at
     * @return true if all chunks the structure's blocks are in from the given offset are currently loaded
     */
    public static boolean isStructureLoaded(Structure structure, IWorld world, BlockPos offset) {
        ChunkPos min = new ChunkPos(offset.add(structure.getMinimumOffset()));
        ChunkPos max = new ChunkPos(offset.add(structure.getMaximumOffset()));
        for (int xx = min.x; xx <= max.x; xx++) {
            for (int zz = min.z; zz <= max.z; zz++) {
                if (!world.getChunkProvider().isChunkLoaded(new ChunkPos(xx, zz))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the lowest mismatching slice of the passed structure in the given world at the given position
     *
     * @param structure the structure to match
     * @param world the world to match in
     * @param offset the offset to match at as center
     * @return the lowest offset-y level where there was a mismatch in the structure, or empty optional if the structure matches.
     */
    public static Optional<Integer> getLowestMismatchingSlice(MatchableStructure structure, IBlockReader world, BlockPos offset) {
        int minY = structure.getMinimumOffset().getY();
        int maxY = structure.getMaximumOffset().getY();
        for (int y = minY; y <= maxY; y++) {
            if (!structure.matchesSlice(world, offset, y)) {
                return Optional.of(y);
            }
        }
        return Optional.empty();
    }

    /**
     * Get all offsets in the structure that do not match the blockstates and/or
     * tileentities in the passed in world at the given offset.
     *
     * @param structure the structure to match
     * @param world the world to test in
     * @param offset the offset to test at
     * @return a set of offsets from the passed offset that do not match
     */
    @Nonnull
    public static Set<BlockPos> getMismatches(MatchableStructure structure, IBlockReader world, BlockPos offset) {
        Set<BlockPos> result = new HashSet<>();
        structure.getContents().forEach((key, value) -> {
            if (!structure.matchesSingleBlock(world, offset, key)) {
                result.add(key);
            }
        });
        return result;
    }
}
