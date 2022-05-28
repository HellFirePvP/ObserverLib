package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ObservableArea;
import hellfirepvp.observerlib.api.ObservableAreaBoundingBox;
import hellfirepvp.observerlib.api.block.BlockChangeSet;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ChangeObserverStructure
 * Created by HellFirePvP
 * Date: 26.04.2019 / 21:33
 */
public class ChangeObserverStructure extends ChangeObserver {

    private final MatchableStructure structure;
    private final ObservableArea observedArea;

    private Set<BlockPos> mismatches = new HashSet<>();

    public ChangeObserverStructure(MatchableStructure structure) {
        super(structure.getRegistryName());
        this.structure = structure;
        this.observedArea = new ObservableAreaBoundingBox(structure.getMinimumOffset(), structure.getMaximumOffset());
    }

    @Override
    public void initialize(LevelAccessor world, BlockPos center) {
        for (BlockPos offset : this.structure.getContents().keySet()) {
            if (!this.structure.matchesSingleBlock(world, center, offset)) {
                this.mismatches.add(offset);
            }
        }
    }

    @Override
    @Nonnull
    public ObservableArea getObservableArea() {
        return this.observedArea;
    }

    @Override
    public boolean notifyChange(Level world, BlockPos center, BlockChangeSet changeSet) {
        for (BlockStateChangeSet.StateChange change : changeSet.getChanges()) {
            if (this.structure.hasBlockAt(change.getRelativePosition()) &&
                    !this.structure.matchesSingleBlock(world, center, change.getRelativePosition(), change.getNewState(),
                            world.getBlockEntity(center.offset(change.getRelativePosition())))) {

                this.mismatches.add(change.getRelativePosition());
            } else {
                this.mismatches.remove(change.getRelativePosition());
            }
        }

        this.mismatches.removeIf(mismatchPos -> !this.structure.hasBlockAt(mismatchPos));
        return this.mismatches.size() <= 0;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        this.mismatches.clear();
        ListTag tagMismatches = tag.getList("mismatchList", Tag.TAG_COMPOUND);

        for (int i = 0; i < tagMismatches.size(); i++) {
            CompoundTag tagPos = tagMismatches.getCompound(i);
            this.mismatches.add(NBTHelper.readBlockPosFromNBT(tagPos));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        ListTag tagMismatches = new ListTag();

        for (BlockPos pos : this.mismatches) {
            CompoundTag tagPos = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(pos, tagPos);
            tagMismatches.add(tagPos);
        }

        tag.put("mismatchList", tagMismatches);
    }

}
