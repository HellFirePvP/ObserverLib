package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;

import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureIntegrityObserver
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:41
 */
public class StructureIntegrityObserver {

    public static void onBlockChange(IWorld world, IChunk chunk, BlockPos pos, BlockState oldS, BlockState newS) {
        if (world.isRemote() ||
                !(world instanceof World) ||
                !chunk.getStatus().isAtLeast(ChunkStatus.FULL)) {
            return;
        }

        StructureMatchingBuffer buf = MatcherObserverHelper.getBuffer((World) world);
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
