package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureIntegrityObserver
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:41
 */
public class StructureIntegrityObserver implements BlockChangeNotifier.Listener {

    @Override
    public void onChange(World world, Chunk chunk, BlockPos pos, BlockState oldState, BlockState newState) {
        if (world.isRemote() || !chunk.getStatus().isAtLeast(ChunkStatus.FULL)) {
            return;
        }

        MatcherObserverHelper.getBuffer(world, buf -> {
            buf.getSubscribers(chunk.getPos(), subscribers -> {
                for (MatchChangeSubscriber<?> subscriber : subscribers) {
                    if (subscriber.observes(pos)) {
                        subscriber.addChange(pos, oldState, newState);
                        buf.markDirty(pos);
                    }
                }

                if (oldState.getBlock() instanceof BlockStructureObserver) {
                    if (((BlockStructureObserver) oldState.getBlock()).removeWithNewState(world, pos, oldState, newState)) {
                        buf.removeSubscriber(pos, result -> {});
                    }
                }
            });
        });
    }
}
