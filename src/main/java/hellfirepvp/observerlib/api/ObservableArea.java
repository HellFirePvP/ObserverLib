package hellfirepvp.observerlib.api;

import com.google.common.collect.Lists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;

/**
 * Objects of this class represent an area an observer checks for changes.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObservableArea
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:16
 */
public interface ObservableArea {

    /**
     * Get the chunks observed with the given center-offset of the observer.
     *
     * @param offset the center offset of the observer
     *
     * @return the chunks this observer's area is watching
     */
    public Collection<ChunkPos> getAffectedChunks(Vector3i offset);

    /**
     * Test if the passed relative position (relative to the observer's center position) is a position
     * which needs to be cached for later processing as it's being observed.
     *
     * @param relativePos the position relative from the observer's center position
     *
     * @return true, if this position is observed by the observer, false if not
     */
    public boolean observes(Vector3i relativePos);

    /**
     * Helper-method to resolve the chunks an observer-box is in.
     */
    default Collection<ChunkPos> calculateAffectedChunks(AxisAlignedBB box, Vector3i offset) {
        return calculateAffectedChunks(
                new Vector3i(Math.round(box.minX + offset.getX()), Math.round(box.minY + offset.getY()), Math.round(box.minZ + offset.getZ())),
                new Vector3i(Math.round(box.maxX + offset.getX()), Math.round(box.maxY + offset.getY()), Math.round(box.maxZ + offset.getZ())));
    }

    /**
     * Helper-method to resolve the chunks an observer-box is in.
     */
    default Collection<ChunkPos> calculateAffectedChunks(Vector3i min, Vector3i max) {
        List<ChunkPos> affected = Lists.newArrayList();
        int maxX = max.getX() >> 4;
        int maxZ = max.getZ() >> 4;
        for (int chX = min.getX() >> 4; chX <= maxX; chX++) {
            for (int chZ = min.getZ() >> 4; chZ <= maxZ; chZ++) {
                affected.add(new ChunkPos(chX, chZ));
            }
        }
        return affected;
    }

}
