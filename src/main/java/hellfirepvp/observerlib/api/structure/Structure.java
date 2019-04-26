package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

/**
 * This class represents a generic structure.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: Structure
 * Created by HellFirePvP
 * Date: 25.04.2019 / 19:45
 */
public interface Structure {

    /**
     * The contents of the change
     *
     * @return a map consisting of offset -> blockstate matcher entries
     */
    public Map<BlockPos, ? extends MatchableState> getContents();

    /**
     * @return the maximum offset any position can be in any x/y/z direction in {@link #getContents()}
     */
    public Vec3i getMaximumOffset();

    /**
     * @return the minimum offset any position can be in any x/y/z direction in {@link #getContents()}
     */
    public Vec3i getMinimumOffset();

    /**
     * Checks if there's a blockstate at the current offset of the change.
     *
     * @param offset the offset to check for a blockstate
     *
     * @return if the change has a state at that offset
     */
    default public boolean hasBlockAt(BlockPos offset) {
        return getContents().containsKey(offset);
    }

}
