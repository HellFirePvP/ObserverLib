package hellfirepvp.observerlib.common.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.structure.ObserverProvider;
import hellfirepvp.observerlib.common.change.MatchChangeSubscriber;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureMatchingBuffer
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:48
 */
public class StructureMatchingBuffer {

    private boolean dirty = false;
    private Map<ChunkPos, List<MatchChangeSubscriber<? extends ChangeObserver>>> subscribers = Maps.newHashMap();
    private Map<BlockPos, MatchChangeSubscriber<? extends ChangeObserver>> requestSubscribers = Maps.newHashMap();

    @Nonnull
    public <T extends ChangeObserver> MatchChangeSubscriber<T> observeArea(IWorld world, BlockPos center, ObserverProvider provider) {
        MatchChangeSubscriber<T> existing;
        if ((existing = (MatchChangeSubscriber<T>) getSubscriber(center)) != null) {
            if (!existing.getObserver().getProviderRegistryName().equals(provider.getRegistryName())) {
                ObserverLib.log.warn("Trying to observe area at dim=" + world.getDimension().getType().getId() + " " + center.toString() +
                        " while it is already being observed by " + existing.getObserver().getProviderRegistryName());
                ObserverLib.log.warn("Removing existing observer!");
                this.removeSubscriber(center);
            } else {
                return existing;
            }
        }

        T observer = (T) provider.provideObserver();
        MatchChangeSubscriber<T> subscriber = new MatchChangeSubscriber<>(center, observer);
        this.requestSubscribers.put(center, subscriber);
        for (ChunkPos pos : subscriber.getObservableChunks()) {
            this.subscribers.computeIfAbsent(pos, (chPos) -> Lists.newArrayList()).add(subscriber);
        }
        observer.initialize(world, center);
        markDirty();
        return subscriber;
    }

    public boolean removeSubscriber(BlockPos pos) {
        if (requestSubscribers.remove(pos) != null) {
            ChunkPos chunk = new ChunkPos(pos);
            List<MatchChangeSubscriber<?>> chunkSubscribers = subscribers
                    .computeIfAbsent(chunk, ch -> Lists.newArrayList());
            chunkSubscribers.clear();
            for (MatchChangeSubscriber<?> subscr : requestSubscribers.values()) {
                if (subscr.getObservableChunks().contains(chunk)) {
                    chunkSubscribers.add(subscr);
                }
            }

            return true;
        }
        return false;
    }

    @Nullable
    public MatchChangeSubscriber<?> getSubscriber(BlockPos pos) {
        return this.requestSubscribers.get(pos);
    }

    @Nonnull
    public List<MatchChangeSubscriber<?>> getSubscribers(ChunkPos pos) {
        return this.subscribers.getOrDefault(pos, Collections.emptyList());
    }

    public final void markDirty() {
        this.dirty = true;
    }

    public final boolean needsSaving() {
        return this.dirty;
    }

    public final void clearDirtyFlag() {
        this.dirty = false;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.subscribers.clear();
        this.requestSubscribers.clear();

        NBTTagList subscriberList = compound.getList("subscribers", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < subscriberList.size(); i++) {
            NBTTagCompound subscriberTag = subscriberList.getCompound(i);

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
            for (ChunkPos chPos : subscriber.getObservableChunks()) {
                this.subscribers.computeIfAbsent(chPos, pos -> Lists.newArrayList())
                        .add(subscriber);
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList subscriberList = new NBTTagList();

        for (MatchChangeSubscriber<? extends ChangeObserver> sub : this.requestSubscribers.values()) {
            NBTTagCompound subscriber = new NBTTagCompound();
            NBTHelper.writeBlockPosToNBT(sub.getCenter(), subscriber);
            subscriber.setString("identifier", sub.getObserver().getProviderRegistryName().toString());

            NBTHelper.setAsSubTag(subscriber, "matchData", sub::writeToNBT);

            subscriberList.add(subscriber);
        }

        compound.setTag("subscribers", subscriberList);
    }

}
