package hellfirepvp.observerlib.common.change;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.api.block.BlockChangeSet;
import hellfirepvp.observerlib.common.api.MatcherObserverHelper;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatchChangeSubscriber
 * Created by HellFirePvP
 * Date: 02.12.2018 / 11:53
 */
public class MatchChangeSubscriber<T extends ChangeObserver<T>> implements ChangeSubscriber<T> {

    public static final Codec<ChangeObserver<?>> OBSERVER_CODEC = RegistryProviders.getRegistry().byNameCodec()
            .<ChangeObserver<?>>dispatch(ChangeObserver::getProvider, ObserverProvider::codec);
    public static final Codec<MatchChangeSubscriber<?>> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BlockPos.CODEC.fieldOf("center").forGetter(MatchChangeSubscriber::getCenter),
            OBSERVER_CODEC.fieldOf("observer").forGetter(MatchChangeSubscriber::getObserver),
            BlockStateChangeSet.CODEC.fieldOf("changeSet").forGetter(subscriber -> subscriber.changeSet),
            Codec.BOOL.optionalFieldOf("isMatching").forGetter(subscriber -> Optional.ofNullable(subscriber.isMatching))
    ).apply(builder, MatchChangeSubscriber::new));

    private final BlockPos center;
    private final T observer;
    private final BlockStateChangeSet changeSet;

    private Boolean isMatching = null;
    private Collection<ChunkPos> affectedChunkCache = null;

    private MatchChangeSubscriber(BlockPos center,
                                  ChangeObserver<?> observer,
                                  BlockStateChangeSet changeSet,
                                  Optional<Boolean> isMatching) {
        this.center = center;
        this.observer = (T) observer;
        this.changeSet = changeSet;
        this.isMatching = isMatching.orElse(null);
    }

    public MatchChangeSubscriber(BlockPos center, T observer) {
        this.center = center;
        this.observer = observer;
        this.changeSet = new BlockStateChangeSet();
    }

    public BlockPos getCenter() {
        return this.center;
    }

    @Override
    @Nonnull
    public T getObserver() {
        return this.observer;
    }

    @Override
    @Nonnull
    public BlockChangeSet getCurrentChangeSet() {
        return this.changeSet;
    }

    public Collection<ChunkPos> getObservableChunks() {
        if (this.affectedChunkCache == null) {
            this.affectedChunkCache = Lists.newArrayList(getObserver().getObservableArea().getAffectedChunks(getCenter()));
        }
        return this.affectedChunkCache;
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

        this.isMatching = this.observer.notifyChange(world, this.getCenter(), this.changeSet);
        this.changeSet.reset();
        MatcherObserverHelper.getBuffer(world).markDirty(this.getCenter());

        return this.isMatching;
    }
}
