package hellfirepvp.observerlib.common.change;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.api.block.BlockChangeSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockStateChangeSet
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:34
 */
public class BlockStateChangeSet implements BlockChangeSet {

    public static final Codec<BlockStateChangeSet> CODEC = BlockStateChange.CODEC.listOf()
            .xmap(BlockStateChangeSet::new, changeSet -> new ArrayList<>(changeSet.changes.values()));

    private final Map<BlockPos, BlockStateChange> changes = Maps.newHashMap();

    public BlockStateChangeSet() {}

    private BlockStateChangeSet(List<BlockStateChange> changes) {
        changes.forEach(change -> this.changes.put(change.getRelativePosition(), change));
    }

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

    public static final class BlockStateChange implements StateChange {

        public static final Codec<BlockStateChange> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(BlockStateChange::getRelativePosition),
                BlockPos.CODEC.fieldOf("abs").forGetter(BlockStateChange::getAbsolutePosition),
                BlockState.CODEC.fieldOf("oldState").forGetter(BlockStateChange::getOldState),
                BlockState.CODEC.fieldOf("newState").forGetter(BlockStateChange::getNewState)
        ).apply(builder, BlockStateChange::new));

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
