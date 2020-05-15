package hellfirepvp.observerlib.common.event;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockChangeNotifier
 * Created by HellFirePvP
 * Date: 15.05.2020 / 16:49
 */
public class BlockChangeNotifier {

    private static Listener[] listeners = new Listener[0];

    public static void addListener(Listener listener) {
        Listener[] copy = new Listener[listeners.length + 1];
        System.arraycopy(listeners, 0, copy, 0, listeners.length);
        copy[listeners.length] = listener;
        listeners = copy;
    }

    public static void onBlockChange(World world, @Nullable Chunk chunk, BlockPos pos, BlockState oldS, BlockState newS) {
        if (chunk == null) {
            chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        }

        for (Listener listener : listeners) {
            listener.onChange(world, chunk, pos, oldS, newS);
        }
    }

    public static interface Listener {

        void onChange(World world, Chunk chunk, BlockPos pos, BlockState oldState, BlockState newState);

    }
}
