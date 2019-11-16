package hellfirepvp.observerlib.api.util;

import hellfirepvp.observerlib.api.block.SimpleMatchableBlockState;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class is an exemplary simple implementation of the structure interface.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArray
 * Created by HellFirePvP
 * Date: 11.08.2019 / 09:10
 */
public class BlockArray implements Structure {

    private Map<BlockPos, MatchableState> blocks = new HashMap<>();
    private Map<BlockPos, MatchableTile<? extends TileEntity>> tiles = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0);
    private Vec3i max = new Vec3i(0, 0, 0);

    @Override
    @Nonnull
    public Map<BlockPos, MatchableState> getContents() {
        return blocks;
    }

    @Nonnull
    @Override
    public Map<BlockPos, ? extends MatchableTile<? extends TileEntity>> getTileEntities() {
        return tiles;
    }

    @Override
    public Vec3i getMaximumOffset() {
        return max;
    }

    @Override
    public Vec3i getMinimumOffset() {
        return min;
    }

    public void addTileEntity(MatchableTile<?> tile, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        tiles.put(pos, tile);
        updateSize(pos);
    }

    public void addBlock(BlockState state, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        MatchableState match = new SimpleMatchableBlockState(state);
        if (state == Blocks.AIR.getDefaultState()) {
            match = MatchableState.IS_AIR;
        }
        blocks.put(pos, match);
        updateSize(pos);
    }

    public void addBlockCube(BlockState state, int ox, int oy, int oz, int tx, int ty, int tz) {
        this.forAllInCube(ox, oy, oz, tx, ty, tz, (x, y, z) -> this.addBlock(state, x, y, z));
    }

    private void forAllInCube(int ox, int oy, int oz, int tx, int ty, int tz, TriConsumer<Integer, Integer, Integer> fct) {
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
                    fct.accept(xx, yy, zz);
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