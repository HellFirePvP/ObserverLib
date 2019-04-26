package hellfirepvp.observerlib.api;

import hellfirepvp.observerlib.api.block.BlockChangeSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

/**
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

    @Nonnull
    public final ResourceLocation getProviderRegistryName() {
        return providerRegistryName;
    }

    public abstract void initialize(IBlockReader world, BlockPos center);

    @Nonnull
    public abstract ObservableArea getObservableArea();

    public abstract boolean notifyChange(IBlockReader world, BlockPos center, BlockChangeSet changeSet);

    public abstract void readFromNBT(NBTTagCompound tag);

    public abstract void writeToNBT(NBTTagCompound tag);

}
