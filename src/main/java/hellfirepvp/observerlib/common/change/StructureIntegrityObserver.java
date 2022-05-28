package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkStatus;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureIntegrityObserver
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:41
 */
public class StructureIntegrityObserver implements BlockChangeNotifier.Listener {

    @Override
    public void onChange(Level world, LevelChunk chunk, BlockPos pos, BlockState oldState, BlockState newState) {
        if (world.isClientSide() ||
                !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            return;
        }

        StructureMatchingBuffer buf = MatcherObserverHelper.getBuffer(world);
        ChunkPos ch = chunk.getPos();
        for (MatchChangeSubscriber<?> subscriber : buf.getSubscribers(ch)) {
            if (subscriber.observes(pos)) {
                subscriber.addChange(pos, oldState, newState);
                buf.markDirty(pos);
            }
        }

        if (oldState.getBlock() instanceof BlockStructureObserver) {
            if (((BlockStructureObserver) oldState.getBlock()).removeWithNewState(world, pos, oldState, newState)) {
                buf.removeSubscriber(pos);
            }
        }
    }
}
