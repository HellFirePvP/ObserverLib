package hellfirepvp.observerlib.api;

import com.google.common.collect.Lists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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

    public boolean observes(BlockPos pos);

    default Collection<ChunkPos> calculateAffectedChunks(AxisAlignedBB box, Vec3i offset) {
        return calculateAffectedChunks(
                new BlockPos(box.minX, box.minY, box.minZ).add(offset),
                new BlockPos(box.maxX, box.maxY, box.maxZ).add(offset));
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
