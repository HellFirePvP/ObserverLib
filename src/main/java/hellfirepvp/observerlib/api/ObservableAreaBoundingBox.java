package hellfirepvp.observerlib.api;

import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3i;

import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObservableAreaBoundingBox
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:12
 */
public class ObservableAreaBoundingBox implements ObservableArea {

    private final AxisAlignedBB box;

    public ObservableAreaBoundingBox(Vector3i min, Vector3i max) {
        this(new AxisAlignedBB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
    }

    public ObservableAreaBoundingBox(AxisAlignedBB boundingBox) {
        this.box = boundingBox;
    }

    @Override
    public Collection<ChunkPos> getAffectedChunks(Vector3i offset) {
        return calculateAffectedChunks(this.box, offset);
    }

    @Override
    public boolean observes(Vector3i relativePos) {
        int x = relativePos.getX();
        int y = relativePos.getY();
        int z = relativePos.getZ();

        return x >= box.minX && x <= box.maxX && y >= box.minY && y <= box.maxY && z >= box.minZ && z <= box.maxZ;
    }

}
