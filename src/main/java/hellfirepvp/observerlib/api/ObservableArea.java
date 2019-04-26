package hellfirepvp.observerlib.api;

import com.google.common.collect.Lists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import java.util.Collection;
import java.util.List;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObservableArea
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:16
 */
public interface ObservableArea {

    public Collection<ChunkPos> getAffectedChunks(Vec3i offset);

    public boolean observes(Vec3i pos);

    default Collection<ChunkPos> calculateAffectedChunks(AxisAlignedBB box, Vec3i offset) {
        return calculateAffectedChunks(
                new Vec3i(Math.round(box.minX + offset.getX()), Math.round(box.minY + offset.getY()), Math.round(box.minZ + offset.getZ())),
                new Vec3i(Math.round(box.maxX + offset.getX()), Math.round(box.maxY + offset.getY()), Math.round(box.minZ + offset.getZ())));
    }

    default Collection<ChunkPos> calculateAffectedChunks(Vec3i min, Vec3i max) {
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
