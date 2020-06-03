package hellfirepvp.observerlib.common.data;

import com.google.common.collect.Maps;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObservableArea;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.api.util.FutureCallback;
import hellfirepvp.observerlib.common.change.MatchChangeSubscriber;
import hellfirepvp.observerlib.common.data.base.SectionWorldData;
import hellfirepvp.observerlib.common.data.base.WorldSection;
import hellfirepvp.observerlib.common.data.io.DirectorySet;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureMatchingBuffer
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:48
 */
public class StructureMatchingBuffer extends SectionWorldData<StructureMatchingBuffer.MatcherSectionData> {

    public StructureMatchingBuffer(WorldCacheDomain.SaveKey<? extends StructureMatchingBuffer> key, DirectorySet directory) {
        super(key, directory, PRECISION_CHUNK);
    }

    @Override
    public MatcherSectionData createNewSection(int sectionX, int sectionZ) {
        return new MatcherSectionData(sectionX, sectionZ);
    }

    @Override
    public void updateTick(World world) {}

    public <T extends ChangeObserver, S extends ChangeSubscriber<T>> void observeArea(IWorld world, BlockPos center, ObserverProvider provider, FutureCallback<S> callback) {
        this.getSubscriber(center, subscriber -> {
            if (subscriber != null) {
                if (!subscriber.getObserver().getProviderRegistryName().equals(provider.getRegistryName())) {
                    ObserverLib.log.warn("Trying to observe area at dim=" + world.getDimension().getType().getId() + " " + center.toString() +
                            " while it is already being observed by " + subscriber.getObserver().getProviderRegistryName());
                    ObserverLib.log.warn("Removing existing observer!");
                    this.removeSubscriber(center, result -> {});
                } else {
                    callback.onSuccess((S) subscriber);
                    return;
                }
            }

            T observer = (T) provider.provideObserver();
            MatchChangeSubscriber<T> newSubscriber = new MatchChangeSubscriber<>(center, observer);

            for (ChunkPos chPos : newSubscriber.getObservableChunks()) {
                getOrCreateSection(chPos.asBlockPos(), sectionData -> {
                    sectionData.addSubscriber(center, newSubscriber);
                    markDirty(sectionData);
                });
            }
            observer.initialize(world, center);
            callback.onSuccess((S) newSubscriber);
        });
    }

    public void removeSubscriber(BlockPos pos, FutureCallback<Boolean> callback) {
        getOrCreateSection(pos, section -> {
            ChangeSubscriber<? extends ChangeObserver> removed = section.removeSubscriber(pos);
            if (removed != null) {
                ObservableArea area = removed.getObserver().getObservableArea();
                for (ChunkPos chPos : area.getAffectedChunks(pos)) {
                    getOrCreateSection(chPos.asBlockPos(), matchData -> {
                        matchData.removeSubscriber(pos);
                        markDirty(matchData);
                    });
                }
            }
            callback.onSuccess(removed != null);
        });
    }

    public void getSubscriber(BlockPos pos, FutureCallback<ChangeSubscriber<? extends ChangeObserver>> callback) {
        getOrCreateSection(pos, section -> callback.onSuccess(section.getSubscriber(pos)));
    }

    public void getSubscribers(ChunkPos pos, FutureCallback<Collection<MatchChangeSubscriber<?>>> callback) {
        getOrCreateSection(pos.asBlockPos(), section -> callback.onSuccess(section.requestSubscribers.values()));
    }

    @Override
    public void writeToNBT(CompoundNBT nbt) {}

    @Override
    public void readFromNBT(CompoundNBT nbt) {}

    public class MatcherSectionData extends WorldSection {

        private Map<BlockPos, MatchChangeSubscriber<? extends ChangeObserver>> requestSubscribers = Maps.newHashMap();

        private MatcherSectionData(int sX, int sZ) {
            super(sX, sZ);
        }

        @Nullable
        private MatchChangeSubscriber<? extends ChangeObserver> getSubscriber(BlockPos pos) {
            return this.requestSubscribers.get(pos);
        }

        @Nullable
        private ChangeSubscriber<? extends ChangeObserver> removeSubscriber(BlockPos pos) {
            return this.requestSubscribers.remove(pos);
        }

        @Nullable
        private ChangeSubscriber<? extends ChangeObserver> addSubscriber(BlockPos pos, MatchChangeSubscriber<? extends ChangeObserver> subscriber) {
            return this.requestSubscribers.put(pos, subscriber);
        }

        @Override
        public boolean isEmpty() {
            return this.requestSubscribers.isEmpty();
        }

        @Override
        public void writeToNBT(CompoundNBT tag) {
            ListNBT subscriberList = new ListNBT();

            for (MatchChangeSubscriber<? extends ChangeObserver> sub : this.requestSubscribers.values()) {
                CompoundNBT subscriber = new CompoundNBT();
                NBTHelper.writeBlockPosToNBT(sub.getCenter(), subscriber);
                subscriber.putString("identifier", sub.getObserver().getProviderRegistryName().toString());

                NBTHelper.setAsSubTag(subscriber, "matchData", sub::writeToNBT);

                subscriberList.add(subscriber);
            }

            tag.put("subscribers", subscriberList);
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            this.requestSubscribers.clear();

            ListNBT subscriberList = tag.getList("subscribers", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < subscriberList.size(); i++) {
                CompoundNBT subscriberTag = subscriberList.getCompound(i);

                BlockPos requester = NBTHelper.readBlockPosFromNBT(subscriberTag);
                ResourceLocation matchIdentifier = new ResourceLocation(subscriberTag.getString("identifier"));
                ObserverProvider observer = RegistryProviders.getProvider(matchIdentifier);
                if (observer == null) {
                    ObserverLib.log.warn("Unknown Observer Provider: " + matchIdentifier.toString() + "! Skipping...");
                    continue;
                }

                MatchChangeSubscriber<?> subscriber = new MatchChangeSubscriber<>(requester, observer.provideObserver());
                subscriber.readFromNBT(subscriberTag.getCompound("matchData"));

                this.requestSubscribers.put(subscriber.getCenter(), subscriber);
            }
        }
    }

}
