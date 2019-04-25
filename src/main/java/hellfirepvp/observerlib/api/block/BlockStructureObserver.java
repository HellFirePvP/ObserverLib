package hellfirepvp.observerlib.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockStructureObserver
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:25
 */
//Implement on Blocks
public interface BlockStructureObserver {

    default boolean removeWithNewState(IBlockReader world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState != newState;
    }

}
