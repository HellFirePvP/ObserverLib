package hellfirepvp.observerlib.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObservableArea;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.change.MatchChangeSubscriber;
import hellfirepvp.observerlib.common.data.base.SectionWorldData;
import hellfirepvp.observerlib.common.data.base.WorldSection;
import hellfirepvp.observerlib.common.util.CodecUtil;
import hellfirepvp.observerlib.common.util.StringCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureMatchingBuffer
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:48
 */
public class StructureMatchingBuffer extends SectionWorldData<StructureMatchingBuffer, StructureMatchingBuffer.MatcherSectionData> {

    public static final Codec<StructureMatchingBuffer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            WorldCacheDomain.SaveKey.CODEC.fieldOf("key").forGetter(CachedWorldData::getSaveKey)
    ).apply(builder, key -> new StructureMatchingBuffer(CodecUtil.cast(key))));

    public StructureMatchingBuffer(WorldCacheDomain.SaveKey<StructureMatchingBuffer> key) {
        super(key, MatcherSectionData.CODEC, PRECISION_CHUNK);
    }

    @Override
    public MatcherSectionData createNewSection(int sectionX, int sectionZ) {
        return new MatcherSectionData(sectionX, sectionZ);
    }

    @Nonnull
    public <T extends ChangeObserver<T>> MatchChangeSubscriber<T> observeArea(Level world, BlockPos center, ObserverProvider<T> provider) {
        MatchChangeSubscriber<T> existing;
        if ((existing = (MatchChangeSubscriber<T>) getSubscriber(center)) != null) {
            if (!existing.getObserver().getProvider().equals(provider)) {
                ObserverLib.log.warn("Trying to observe area at dim={} {} while it is already being observed by {}",
                        world.dimension().location(), center.toString(), existing.getObserver().getProvider());
                ObserverLib.log.warn("Removing existing observer!");
                this.write(() -> this.removeSubscriber(center));
            } else {
                return existing;
            }
        }

        T observer = provider.newObserver();
        MatchChangeSubscriber<T> subscriber = new MatchChangeSubscriber<>(center, observer);

        for (ChunkPos chPos : subscriber.getObservableChunks()) {
            MatcherSectionData data = getOrCreateSection(chPos.getWorldPosition());
            this.write(() -> data.addSubscriber(center, subscriber));
            markDirty(data);
        }
        observer.initialize(world, center);
        return subscriber;
    }

    public boolean removeSubscriber(BlockPos pos) {
        MatcherSectionData data = getOrCreateSection(pos);

        ChangeSubscriber<? extends ChangeObserver<?>> removed = this.write(() -> data.removeSubscriber(pos));
        if (removed != null) {
            ObservableArea area = removed.getObserver().getObservableArea();
            for (ChunkPos chPos : area.getAffectedChunks(pos)) {
                MatcherSectionData matchData = getOrCreateSection(chPos.getWorldPosition());
                this.write(() -> matchData.removeSubscriber(pos));
                markDirty(matchData);
            }
        }
        return removed != null;
    }

    @Nullable
    public ChangeSubscriber<? extends ChangeObserver<?>> getSubscriber(BlockPos pos) {
        MatcherSectionData section = this.getSection(pos);
        if (section == null) return null;
        return this.read(() -> section.getSubscriber(pos));
    }

    @Nonnull
    public Collection<MatchChangeSubscriber<?>> getSubscribers(ChunkPos pos) {
        MatcherSectionData section = this.getSection(pos.getWorldPosition());
        if (section == null) return List.of();
        return this.read(() -> {
            return new ArrayList<>(section.requestSubscribers.values());
        });
    }

    public static class MatcherSectionData extends WorldSection {

        public static final Codec<MatcherSectionData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("sX").forGetter(WorldSection::getSectionX),
                Codec.INT.fieldOf("sZ").forGetter(WorldSection::getSectionZ),
                Codec.unboundedMap(StringCodecs.blockPos(), MatchChangeSubscriber.CODEC).fieldOf("subscribers")
                        .forGetter(section -> section.requestSubscribers)
        ).apply(builder, MatcherSectionData::new));

        private final Map<BlockPos, MatchChangeSubscriber<? extends ChangeObserver<?>>> requestSubscribers = new HashMap<>();

        private MatcherSectionData(int sX, int sZ) {
            super(sX, sZ);
        }

        private MatcherSectionData(int sX, int sZ, Map<BlockPos, MatchChangeSubscriber<? extends ChangeObserver<?>>> subscribers) {
            super(sX, sZ);
            this.requestSubscribers.putAll(subscribers);
        }

        @Nullable
        private MatchChangeSubscriber<? extends ChangeObserver<?>> getSubscriber(BlockPos pos) {
            return this.requestSubscribers.get(pos);
        }

        @Nullable
        private ChangeSubscriber<? extends ChangeObserver<?>> removeSubscriber(BlockPos pos) {
            return this.requestSubscribers.remove(pos);
        }

        @Nullable
        private ChangeSubscriber<? extends ChangeObserver<?>> addSubscriber(BlockPos pos, MatchChangeSubscriber<? extends ChangeObserver<?>> subscriber) {
            return this.requestSubscribers.put(pos, subscriber);
        }
    }

}
