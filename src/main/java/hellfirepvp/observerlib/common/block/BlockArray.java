package hellfirepvp.observerlib.common.block;

import hellfirepvp.observerlib.api.block.MatchableBlockState;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
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

    private Map<BlockPos, MatchableState> blocks = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0);
    private Vec3i max = new Vec3i(0, 0, 0);

    @Override
    public Map<BlockPos, MatchableState> getContents() {
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

    public void addBlock(BlockPos pos, IBlockState state) {
        MatchableState match = new MatchableBlockState(state);
        if (state == Blocks.AIR.getDefaultState()) {
            match = MatchableState.IS_AIR;
        }
        blocks.put(pos, match);
        updateSize(pos);
    }

    public void addBlock(int x, int y, int z, IBlockState state) {
        this.addBlock(new BlockPos(x, y, z), state);
    }

    public void addBlockCube(IBlockState state, int ox, int oy, int oz, int tx, int ty, int tz) {
        int lx, ly, lz;
        int hx, hy, hz;
        if(ox < tx) {
            lx = ox;
            hx = tx;
        } else {
            lx = tx;
            hx = ox;
        }
        if(oy < ty) {
            ly = oy;
            hy = ty;
        } else {
            ly = ty;
            hy = oy;
        }
        if(oz < tz) {
            lz = oz;
            hz = tz;
        } else {
            lz = tz;
            hz = oz;
        }

        for (int xx = lx; xx <= hx; xx++) {
            for (int zz = lz; zz <= hz; zz++) {
                for (int yy = ly; yy <= hy; yy++) {
                    this.addBlock(new BlockPos(xx, yy, zz), state);
                }
            }
        }
    }

    private void updateSize(BlockPos addedPos) {
        if(addedPos.getX() < min.getX()) {
            min = new Vec3i(addedPos.getX(), min.getY(), min.getZ());
        }
        if(addedPos.getX() > max.getX()) {
            max = new Vec3i(addedPos.getX(), max.getY(), max.getZ());
        }
        if(addedPos.getY() < min.getY()) {
            min = new Vec3i(min.getX(), addedPos.getY(), min.getZ());
        }
        if(addedPos.getY() > max.getY()) {
            max = new Vec3i(max.getX(), addedPos.getY(), max.getZ());
        }
        if(addedPos.getZ() < min.getZ()) {
            min = new Vec3i(min.getX(), min.getY(), addedPos.getZ());
        }
        if(addedPos.getZ() > max.getZ()) {
            max = new Vec3i(max.getX(), max.getY(), addedPos.getZ());
        }
    }

}
