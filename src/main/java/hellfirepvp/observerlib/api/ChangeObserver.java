package hellfirepvp.observerlib.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ChangeObserver
 * Created by HellFirePvP
 * Date: 23.04.2019 / 22:15
 */
public abstract class ChangeObserver implements IForgeRegistryEntry<ChangeObserver> {

    private final ResourceLocation registryName;

    public ChangeObserver(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Override
    public Class<ChangeObserver> getRegistryType() {
        return ChangeObserver.class;
    }

    @Override
    public final ChangeObserver setRegistryName(ResourceLocation name) {
        return this;
    }

    @Nullable
    @Override
    public final ResourceLocation getRegistryName() {
        return registryName;
    }

    public abstract ObservableArea getObservableArea();

    public abstract boolean notifyChange(IBlockReader world, BlockPos centre, BlockChangeSet changeSet);

    public abstract void readFromNBT(NBTTagCompound tag);

    public abstract void writeToNBT(NBTTagCompound tag);

}
