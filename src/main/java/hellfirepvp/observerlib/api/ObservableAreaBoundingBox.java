package hellfirepvp.observerlib.api;

import net.minecraft.util.math.*;

import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObservableAreaBoundingBox
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:12
 */
public class ObservableAreaBoundingBox implements ObservableArea {

    private final AxisAlignedBB boundingBox;

    public ObservableAreaBoundingBox(Vec3i min, Vec3i max) {
        this(new AxisAlignedBB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
    }

    public ObservableAreaBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public Collection<ChunkPos> getAffectedChunks(Vec3i offset) {
        return calculateAffectedChunks(this.boundingBox, offset);
    }

    @Override
    public boolean observes(Vec3i pos) {
        return boundingBox.contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }

}
