package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * This class represents a generic structure.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: Structure
 * Created by HellFirePvP
 * Date: 25.04.2019 / 19:45
 */
public interface Structure {

    /**
     * The contents of the structure
     *
     * @return a map consisting of offset -> blockstate matcher entries
     */
    @Nonnull
    public Map<BlockPos, ? extends MatchableState> getContents();

    /**
     * The tiles of the structure
     *
     * Will only be queried/accessed for positions where a MatchableState
     * {@link MatchableState#createTileEntity(IBlockReader, long)} creates a tileentity
     *
     * @return a map consisting of offset -> tileentity matching and data entries
     */
    @Nonnull
    public Map<BlockPos, ? extends MatchableTile<? extends TileEntity>> getTileEntities();

    /**
     * @return the maximum offset any position can be in any x/y/z direction in {@link #getContents()}
     */
    public Vec3i getMaximumOffset();

    /**
     * @return the minimum offset any position can be in any x/y/z direction in {@link #getContents()}
     */
    public Vec3i getMinimumOffset();

    /**
     * Checks if there's a blockstate at the current offset of the change.
     *
     * @param offset the offset to check for a blockstate
     *
     * @return if the change has a state at that offset
     */
    default public boolean hasBlockAt(BlockPos offset) {
        return getContents().containsKey(offset);
    }

}
