package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureIntegrityObserver
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:41
 */
public class StructureIntegrityObserver {

    public static void onBlockChange(World world, @Nullable Chunk chunk, BlockPos pos, BlockState oldS, BlockState newS) {
        if (chunk == null) {
            chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        }
        if (world.isRemote() || !chunk.getStatus().isAtLeast(ChunkStatus.FULL)) {
            return;
        }

        StructureMatchingBuffer buf = MatcherObserverHelper.getBuffer(world);
        ChunkPos ch = chunk.getPos();
        Collection<MatchChangeSubscriber<?>> subscribers = buf.getSubscribers(ch);
        for (MatchChangeSubscriber<?> subscriber : subscribers) {
            if (subscriber.observes(pos)) {
                subscriber.addChange(pos, oldS, newS);
                buf.markDirty(pos);
            }
        }

        if (oldS.getBlock() instanceof BlockStructureObserver) {
            if (((BlockStructureObserver) oldS.getBlock()).removeWithNewState(world, pos, oldS, newS)) {
                buf.removeSubscriber(pos);
            }
        }
    }

}
