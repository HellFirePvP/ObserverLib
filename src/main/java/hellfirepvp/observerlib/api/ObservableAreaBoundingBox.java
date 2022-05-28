package hellfirepvp.observerlib.api;

import net.minecraft.core.Vec3i;

import java.util.Collection;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObservableAreaBoundingBox
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:12
 */
public class ObservableAreaBoundingBox implements ObservableArea {

    private final AABB box;

    public ObservableAreaBoundingBox(Vec3i min, Vec3i max) {
        this(new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
    }

    public ObservableAreaBoundingBox(AABB boundingBox) {
        this.box = boundingBox;
    }

    @Override
    public Collection<ChunkPos> getAffectedChunks(Vec3i offset) {
        return calculateAffectedChunks(this.box, offset);
    }

    @Override
    public boolean observes(Vec3i relativePos) {
        int x = relativePos.getX();
        int y = relativePos.getY();
        int z = relativePos.getZ();

        return x >= box.minX && x <= box.maxX && y >= box.minY && y <= box.maxY && z >= box.minZ && z <= box.maxZ;
    }

}
