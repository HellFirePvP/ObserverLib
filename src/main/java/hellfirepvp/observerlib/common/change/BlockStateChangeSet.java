package hellfirepvp.observerlib.common.change;

import com.google.common.collect.Maps;
import hellfirepvp.observerlib.api.block.BlockChangeSet;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import hellfirepvp.observerlib.api.block.BlockChangeSet.StateChange;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockStateChangeSet
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:34
 */
public class BlockStateChangeSet implements BlockChangeSet {

    private final Map<BlockPos, BlockStateChange> changes = Maps.newHashMap();

    public void addChange(BlockPos pos, BlockPos absolute, BlockState oldState, BlockState newState) {
        BlockStateChange oldChangeSet = this.changes.get(pos);
        if (oldChangeSet != null) { // Chain changes so absolute old one is still consistent!
            this.changes.put(pos, new BlockStateChange(pos, absolute, oldChangeSet.oldState, newState));
        } else {
            this.changes.put(pos, new BlockStateChange(pos, absolute, oldState, newState));
        }
    }

    public final void reset() {
        this.changes.clear();
    }

    @Override
    public boolean hasChange(BlockPos pos) {
        return this.changes.containsKey(pos);
    }

    @Override
    public boolean isEmpty() {
        return this.changes.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<StateChange> getChanges() {
        return Collections.unmodifiableCollection(this.changes.values());
    }

    public void readFromNBT(CompoundTag cmp) {
        this.changes.clear();

        ListTag changeList = cmp.getList("changeList", Tag.TAG_COMPOUND);
        for (int i = 0; i < changeList.size(); i++) {
            CompoundTag changeTag = changeList.getCompound(i);

            BlockPos pos = NBTHelper.readBlockPosFromNBT(changeTag.getCompound("relPos"));
            BlockPos abs = NBTHelper.readBlockPosFromNBT(changeTag.getCompound("absPos"));
            BlockState oldState = NBTHelper.getBlockStateFromTag(changeTag.getCompound("oldState"),
                    Blocks.AIR.defaultBlockState());
            BlockState newState = NBTHelper.getBlockStateFromTag(changeTag.getCompound("newState"),
                    Blocks.AIR.defaultBlockState());
            this.changes.put(pos, new BlockStateChange(pos, abs, oldState, newState));
        }
    }

    public void writeToNBT(CompoundTag cmp) {
        ListTag changes = new ListTag();
        for (BlockStateChange change : this.changes.values()) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.setAsSubTag(tag, "relPos", (posTag) -> NBTHelper.writeBlockPosToNBT(change.getRelativePosition(), posTag));
            NBTHelper.setAsSubTag(tag, "absPos", (posTag) -> NBTHelper.writeBlockPosToNBT(change.getAbsolutePosition(), posTag));
            NBTHelper.writeBlockPosToNBT(change.pos, tag);
            tag.put("oldState", NBTHelper.getBlockStateNBTTag(change.oldState));
            tag.put("newState", NBTHelper.getBlockStateNBTTag(change.newState));
            changes.add(tag);
        }

        cmp.put("changeList", changes);
    }

    public static final class BlockStateChange implements StateChange {

        private final BlockPos pos, abs;
        private final BlockState oldState, newState;

        private BlockStateChange(BlockPos pos, BlockPos abs, BlockState oldState, BlockState newState) {
            this.pos = pos;
            this.abs = abs;
            this.oldState = oldState;
            this.newState = newState;
        }

        @Nonnull
        @Override
        public BlockPos getAbsolutePosition() {
            return this.abs;
        }

        @Nonnull
        @Override
        public BlockPos getRelativePosition() {
            return this.pos;
        }

        @Nonnull
        @Override
        public BlockState getOldState() {
            return this.oldState;
        }

        @Nonnull
        @Override
        public BlockState getNewState() {
            return this.newState;
        }
    }

}
