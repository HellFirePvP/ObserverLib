package hellfirepvp.observerlib.api;

import hellfirepvp.observerlib.api.block.BlockChangeSet;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * The template for Observers.
 *
 * Hold any necessary data to determine validity of your current state here.
 * For example for structures, necessary information would be which parts of the structure are not valid (yet).
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ChangeObserver
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:15
 */
public abstract class ChangeObserver {

    private final ResourceLocation providerRegistryName;

    public ChangeObserver(ResourceLocation providerRegistryName) {
        this.providerRegistryName = providerRegistryName;
    }

    /**
     * Returns the registry name of the owning {@link ObserverProvider}.
     * Used for serialization/deserialization so the observer can be saved persistently.
     *
     * @return the changeprovider's registry name
     */
    @Nonnull
    public final ResourceLocation getProviderRegistryName() {
        return providerRegistryName;
    }

    /**
     * Called once after the observer is newly set on a position to observe it.
     * Useful to gather initial data about the state and the observer's surroundings.
     *
     * Called from {@link ObserverHelper#observeArea(Level, BlockPos, ObserverProvider)}.
     *
     * @param world the world the observer will be observing changes in
     * @param center the current offset/center of where the observer is located at
     */
    public abstract void initialize(LevelAccessor world, BlockPos center);

    /**
     * The ObservableArea the observer is checking for changes in the world.
     *
     * This ObservableArea is not offset by the center, so its always centered at 0, 0, 0 so to speak.
     * It is to be shifted/queried at the accurate position on demand.
     * See {@link ObservableArea#getAffectedChunks(Vec3i)}.
     *
     * @return the observable area
     */
    @Nonnull
    public abstract ObservableArea getObservableArea();

    /**
     * Update this observer's state of validity in the world.
     *
     * Called from the {@link ChangeSubscriber} ONLY IF there's changes the current BlockChangeSet.
     * These changes are set to happen in positions observed by the observer's {@link ObservableArea}.
     *
     * @param world the world the observer is in
     * @param center the center the observer is at
     * @param changeSet the current changeSet containing information about what BlockStates have changed.
     *
     * @return true, if this current state is now considered valid, false otherwise
     */
    public abstract boolean notifyChange(Level world, BlockPos center, BlockChangeSet changeSet);

    /**
     * Read persistent information back into this observer.
     *
     * @param tag the tag holding persistent information about this observer
     */
    public abstract void readFromNBT(CompoundTag tag);

    /**
     * Write information of this observer for persistence.
     * The tag passed here is empty and will not be further modified after this method before being saved.
     *
     * @param tag an empty TagCompound to write information into
     */
    public abstract void writeToNBT(CompoundTag tag);

}
