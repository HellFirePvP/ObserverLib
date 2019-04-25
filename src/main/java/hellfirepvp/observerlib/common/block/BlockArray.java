package hellfirepvp.observerlib.common.block;

import hellfirepvp.observerlib.api.block.MatchableBlockState;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArray
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:50
 */
public class BlockArray implements Structure {

    private Map<BlockPos, MatchableBlockState> blocks = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0);
    private Vec3i max = new Vec3i(0, 0, 0);

    @Override
    public Map<BlockPos, MatchableBlockState> getContents() {
        return blocks;
    }

    @Override
    public Vec3i getMaximumOffset() {
        return max;
    }

    @Override
    public Vec3i getMinimumOffset() {
        return min;
    }

}
