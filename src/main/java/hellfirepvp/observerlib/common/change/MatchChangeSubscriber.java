package hellfirepvp.observerlib.common.change;

import com.google.common.collect.Lists;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.block.BlockChangeSet;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatchChangeSubscriber
 * Created by HellFirePvP
 * Date: 02.12.2018 / 11:53
 */
public class MatchChangeSubscriber<T extends ChangeObserver> implements ChangeSubscriber<T> {

    private BlockPos center;
    private final T matcher;

    private final BlockStateChangeSet changeSet = new BlockStateChangeSet();
    private Boolean isMatching = null;

    private Collection<ChunkPos> affectedChunkCache = null;

    public MatchChangeSubscriber(BlockPos center, T matcher) {
        this.center = center;
        this.matcher = matcher;
    }

    public BlockPos getCenter() {
        return center;
    }

    @Override
    @Nonnull
    public BlockChangeSet getCurrentChangeSet() {
        return this.changeSet;
    }

    @Override
    @Nonnull
    public T getObserver() {
        return matcher;
    }

    public Collection<ChunkPos> getObservableChunks() {
        if (affectedChunkCache == null) {
            affectedChunkCache = Lists.newArrayList(getObserver().getObservableArea().getAffectedChunks(getCenter()));
        }
        return affectedChunkCache;
    }

    public boolean observes(BlockPos pos) {
        return this.getObserver().getObservableArea().observes(pos.subtract(getCenter()));
    }

    public void addChange(BlockPos pos, BlockState oldState, BlockState newState) {
        this.changeSet.addChange(pos.subtract(getCenter()), pos, oldState, newState);
    }

    @Override
    public boolean isValid(Level world) {
        if (this.isMatching != null && this.changeSet.isEmpty()) {
            return isMatching;
        }

        this.isMatching = this.matcher.notifyChange(world, this.getCenter(), this.changeSet);
        this.changeSet.reset();
        MatcherObserverHelper.getBuffer(world).markDirty(this.getCenter());

        return this.isMatching;
    }

    public void readFromNBT(CompoundTag tag) {
        this.affectedChunkCache = null;

        this.matcher.readFromNBT(tag.getCompound("matchData"));
        this.changeSet.readFromNBT(tag.getCompound("changeData"));
        this.center = NBTHelper.readBlockPosFromNBT(tag);
        if (tag.contains("isMatching")) {
            this.isMatching = tag.getBoolean("isMatching");
        } else {
            this.isMatching = null;
        }
    }

    public void writeToNBT(CompoundTag tag) {
        NBTHelper.setAsSubTag(tag, "matchData", this.matcher::writeToNBT);
        NBTHelper.setAsSubTag(tag, "changeData", this.changeSet::writeToNBT);

        NBTHelper.writeBlockPosToNBT(this.center, tag);
        if (this.isMatching != null) {
            tag.putBoolean("isMatching", this.isMatching);
        }
    }
}
