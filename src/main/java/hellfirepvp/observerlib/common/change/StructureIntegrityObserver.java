package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import hellfirepvp.observerlib.common.event.BlockModifyEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureIntegrityObserver
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:41
 */
public class StructureIntegrityObserver {

    public StructureIntegrityObserver(IEventBus eventBus) {
        eventBus.addListener(this::onBlockChange);
    }

    private void onBlockChange(BlockModifyEvent event) {
        IWorld world = event.getWorld();
        if (world.isRemote() ||
                !(world instanceof World) ||
                !event.getChunk().getStatus().isAtLeast(ChunkStatus.POSTPROCESSED)) {
            return;
        }

        StructureMatchingBuffer buf = MatcherObserverHelper.getBuffer((World) world);
        ChunkPos ch = event.getChunk().getPos();
        BlockPos pos = event.getPos();
        IBlockState oldS = event.getOldState();
        IBlockState newS = event.getNewState();

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
