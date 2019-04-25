package hellfirepvp.observerlib.common.change;

import com.google.common.collect.Maps;
import hellfirepvp.observerlib.api.BlockChangeSet;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockStateChangeSet
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:34
 */
public class BlockStateChangeSet implements BlockChangeSet {

    private Map<BlockPos, BlockStateChange> changes = Maps.newHashMap();

    public void addChange(BlockPos pos, BlockPos absolute, IBlockState oldState, IBlockState newState) {
        BlockStateChange oldChangeSet = this.changes.get(pos);
        if (oldChangeSet != null) { // Chain changes so absolute old one is still consistent!
            this.changes.put(pos, new BlockStateChange(pos, absolute, oldChangeSet.oldState, newState));
        } else {
            this.changes.put(pos, new BlockStateChange(pos, absolute, oldState, newState));
        }
    }

    @Override
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

    public void readFromNBT(NBTTagCompound cmp) {
        this.changes.clear();

        NBTTagList changeList = cmp.getList("changeList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < changeList.size(); i++) {
            NBTTagCompound changeTag = changeList.getCompound(i);

            BlockPos pos = NBTHelper.readBlockPosFromNBT(changeTag.getCompound("relPos"));
            BlockPos abs = NBTHelper.readBlockPosFromNBT(changeTag.getCompound("absPos"));
            IBlockState oldState = NBTHelper.getBlockStateFromTag(changeTag.getCompound("oldState"),
                    Blocks.AIR.getDefaultState());
            IBlockState newState = NBTHelper.getBlockStateFromTag(changeTag.getCompound("newState"),
                    Blocks.AIR.getDefaultState());
            this.changes.put(pos, new BlockStateChange(pos, abs, oldState, newState));
        }
    }

    public void writeToNBT(NBTTagCompound cmp) {
        NBTTagList changes = new NBTTagList();
        for (BlockStateChange change : this.changes.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTHelper.setAsSubTag(tag, "relPos", (posTag) -> NBTHelper.writeBlockPosToNBT(change.getRelativePosition(), posTag));
            NBTHelper.setAsSubTag(tag, "absPos", (posTag) -> NBTHelper.writeBlockPosToNBT(change.getAbsolutePosition(), posTag));
            NBTHelper.writeBlockPosToNBT(change.pos, tag);
            tag.setTag("oldState", NBTHelper.getBlockStateNBTTag(change.oldState));
            tag.setTag("newState", NBTHelper.getBlockStateNBTTag(change.newState));
            changes.add(tag);
        }

        cmp.setTag("changeList", changes);
    }

    public static final class BlockStateChange implements StateChange {

        private final BlockPos pos, abs;
        private final IBlockState oldState, newState;

        private BlockStateChange(BlockPos pos, BlockPos abs, IBlockState oldState, IBlockState newState) {
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
        public IBlockState getOldState() {
            return this.oldState;
        }

        @Nonnull
        @Override
        public IBlockState getNewState() {
            return this.newState;
        }
    }

}
