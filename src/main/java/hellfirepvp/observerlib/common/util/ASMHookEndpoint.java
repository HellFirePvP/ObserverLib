package hellfirepvp.observerlib.common.util;

import hellfirepvp.observerlib.common.change.StructureIntegrityObserver;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ASMHookEndpoint
 * Created by HellFirePvP
 * Date: 05.07.2019 / 12:12
 */
public class ASMHookEndpoint {

    public static void onBlockChange(World world, BlockPos pos, @Nullable Chunk chunk, BlockState prevState, BlockState postState) {
        StructureIntegrityObserver.onBlockChange(world, chunk, pos, prevState, postState);
    }

}
