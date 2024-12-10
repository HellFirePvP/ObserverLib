package hellfirepvp.observerlib.common.change;

import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ObservableArea;
import hellfirepvp.observerlib.api.ObservableAreaBoundingBox;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.api.block.BlockChangeSet;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ChangeObserverStructure
 * Created by HellFirePvP
 * Date: 26.04.2019 / 21:33
 */
public class ChangeObserverStructure extends ChangeObserver<ChangeObserverStructure> {

    private final ObserverProviderStructure provider;
    private final MatchableStructure structure;
    private final ObservableArea observedArea;

    private final Set<BlockPos> mismatches = new HashSet<>();

    public ChangeObserverStructure(ObserverProviderStructure provider, MatchableStructure structure) {
        this.provider = provider;
        this.structure = structure;
        this.observedArea = new ObservableAreaBoundingBox(structure.getMinimumOffset(), structure.getMaximumOffset());
    }

    ChangeObserverStructure addMismatches(List<BlockPos> mismatches) {
        this.mismatches.addAll(mismatches);
        return this;
    }

    List<BlockPos> getMismatches() {
        return new ArrayList<>(this.mismatches);
    }

    @Override
    public ObserverProviderStructure getProvider() {
        return this.provider;
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
        return this.mismatches.isEmpty();
    }
}
