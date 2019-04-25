package hellfirepvp.observerlib.common.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.structure.ObserverProvider;
import hellfirepvp.observerlib.common.change.ChangeSubscriber;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
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
    private Map<ChunkPos, List<ChangeSubscriber<? extends ChangeObserver>>> subscribers = Maps.newHashMap();
    private Map<BlockPos, ChangeSubscriber<? extends ChangeObserver>> requestSubscribers = Maps.newHashMap();

    //@Nonnull
    //public ChangeSubscriber<StructureMatcherPatternArray> observeAndInitializePattern(IBlockReader world,
    //                                                                                  BlockPos center,
    //                                                                                  MatchableStructure structure) {
    //    StructureMatcherPatternArray match = new StructureMatcherPatternArray(structure.getRegistryName());
    //    match.initialize(world, center);
    //    return observeArea(center, match);
    //}

    @Nonnull
    public <T extends ChangeObserver> ChangeSubscriber<T> observeArea(BlockPos requester, T matcher) {
        ObserverProvider observer = RegistryProviders.getProvider(matcher.getRegistryName());
        if (observer == null) {
            ObserverLib.log.warn("Found unregistered change matcher: " + matcher.getRegistryName().toString());
            ObserverLib.log.warn("It will NOT persist! Register your matchers!");
        }
        ChangeSubscriber<T> subscriber = new ChangeSubscriber<>(requester, matcher);
        this.requestSubscribers.put(requester, subscriber);
        for (ChunkPos pos : subscriber.getObservableChunks()) {
            this.subscribers.computeIfAbsent(pos, (chPos) -> Lists.newArrayList()).add(subscriber);
        }

        markDirty();
        return subscriber;
    }

    public boolean removeSubscriber(BlockPos pos) {
        if (requestSubscribers.remove(pos) != null) {
            ChunkPos chunk = new ChunkPos(pos);
            List<ChangeSubscriber<?>> chunkSubscribers = subscribers
                    .computeIfAbsent(chunk, ch -> Lists.newArrayList());
            chunkSubscribers.clear();
            for (ChangeSubscriber<?> subscr : requestSubscribers.values()) {
                if (subscr.getObservableChunks().contains(chunk)) {
                    chunkSubscribers.add(subscr);
                }
            }

            return true;
        }
        return false;
    }

    @Nullable
    public ChangeSubscriber<?> getSubscriber(BlockPos pos) {
        return this.requestSubscribers.get(pos);
    }

    @Nonnull
    public List<ChangeSubscriber<?>> getSubscribers(ChunkPos pos) {
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
                ObserverLib.log.warn("Unknown StructureMatcher: " + matchIdentifier.toString() + "! Skipping...");
                continue;
            }

            ChangeSubscriber<?> subscriber = new ChangeSubscriber<>(requester, observer.provideMatcher());
            subscriber.readFromNBT(subscriberTag.getCompound("matchData"));

            this.requestSubscribers.put(subscriber.getRequester(), subscriber);
            for (ChunkPos chPos : subscriber.getObservableChunks()) {
                this.subscribers.computeIfAbsent(chPos, pos -> Lists.newArrayList())
                        .add(subscriber);
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList subscriberList = new NBTTagList();

        for (ChangeSubscriber<? extends ChangeObserver> sub : this.requestSubscribers.values()) {
            NBTTagCompound subscriber = new NBTTagCompound();
            NBTHelper.writeBlockPosToNBT(sub.getRequester(), subscriber);
            subscriber.setString("identifier", sub.getMatcher().getRegistryName().toString());

            NBTHelper.setAsSubTag(subscriber, "matchData", sub::writeToNBT);

            subscriberList.add(subscriber);
        }

        compound.setTag("subscribers", subscriberList);
    }

}
