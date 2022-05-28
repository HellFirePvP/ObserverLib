package hellfirepvp.observerlib.api.structure;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.tile.MatchableTile;
import hellfirepvp.observerlib.api.util.ContentSerializable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents a generic structure.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: Structure
 * Created by HellFirePvP
 * Date: 25.04.2019 / 19:45
 */
public interface Structure extends ContentSerializable {

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
     * {@link MatchableState#createTileEntity(BlockGetter, BlockPos, long)} creates a tileentity
     *
     * @return a map consisting of offset -> tileentity matching and data entries
     */
    @Nonnull
    public Map<BlockPos, ? extends MatchableTile<? extends BlockEntity>> getTileEntities();

    /**
     * @return the maximum offset any position can be in any x/y/z direction in {@link #getContents()}
     */
    public Vec3i getMaximumOffset();

    /**
     * @return the minimum offset any position can be in any x/y/z direction in {@link #getContents()}
     */
    public Vec3i getMinimumOffset();

    /**
     * Checks if there's a blockstate at the current offset.
     *
     * @param offset the offset to check for a blockstate
     *
     * @return if the structure has a state at that offset
     */
    default public boolean hasBlockAt(BlockPos offset) {
        return getContents().containsKey(offset);
    }

    /**
     * Returns the blockstate at the given offset.
     * Returns {@link MatchableState#AIR} if not present.
     *
     * @param offset the offset to get the blockstate at
     *
     * @return the blockstate at that position or AIR
     */
    @Nonnull
    default public MatchableState getBlockStateAt(BlockPos offset) {
        if (!hasBlockAt(offset)) {
            return MatchableState.AIR;
        }
        return ObjectUtils.firstNonNull(getContents().get(offset), MatchableState.AIR);
    }

    /**
     * Checks if there's a tileentity supposed to be set at the current offset.
     *
     * @param offset the offset to check for a tileentity
     *
     * @return if the structure has a tileentity at that offset
     */
    default public boolean hasTileAt(BlockPos offset) {
        return getTileEntities().containsKey(offset);
    }

    /**
     * Returns the expected tileentity at the given offset.
     *
     * @param offset the offset to get the tileentity at
     *
     * @return the matchable tileentity or null if not present
     */
    @Nullable
    default public MatchableTile<? extends BlockEntity> getTileEntityAt(BlockPos offset) {
        if (!hasTileAt(offset)) {
            return null;
        }
        return getTileEntities().get(offset);
    }

    /**
     * Returns a slice of the structure of the given y-level.
     *
     * @param yOffset the y-level of the structure slice
     *
     * @return offsets and their associated expected states of that slice
     */
    default public List<Tuple<BlockPos, ? extends MatchableState>> getStructureSlice(int yOffset) {
        return getContents().entrySet().stream()
                .filter(e -> e.getKey().getY() == yOffset)
                .map(e -> new Tuple<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
