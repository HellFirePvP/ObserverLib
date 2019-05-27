package hellfirepvp.observerlib.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTHelper
 * Created by HellFirePvP
 * Date: 25.04.2019 / 21:04
 */
public class NBTHelper {

    @Nonnull
    public static NBTTagCompound getBlockStateNBTTag(IBlockState state) {
        if(state.getBlock().getRegistryName() == null) {
            state = Blocks.AIR.getDefaultState();
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("registryName", state.getBlock().getRegistryName().toString());
        NBTTagList properties = new NBTTagList();
        for (IProperty property : state.getProperties()) {
            NBTTagCompound propTag = new NBTTagCompound();
            try {
                propTag.setString("value", property.getName(state.get(property)));
            } catch (Exception exc) {
                continue;
            }
            propTag.setString("property", property.getName());
            properties.add(propTag);
        }
        tag.setTag("properties", properties);
        return tag;
    }

    @Nullable
    public static <T extends Comparable<T>> IBlockState getBlockStateFromTag(NBTTagCompound cmp, IBlockState _default) {
        ResourceLocation key = new ResourceLocation(cmp.getString("registryName"));
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if(block == null || block == Blocks.AIR) return _default;
        IBlockState state = block.getDefaultState();
        Collection<IProperty<?>> properties = state.getProperties();
        NBTTagList list = cmp.getList("properties", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound propertyTag = list.getCompound(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            IProperty<T> match = (IProperty<T>) iterativeSearch(properties, prop -> prop.getName().equalsIgnoreCase(propertyStr));
            if(match != null) {
                try {
                    Optional<T> opt = match.parseValue(valueStr);
                    if(opt.isPresent()) {
                        state = state.with(match, opt.get());
                    }
                } catch (Throwable tr) {} // Thanks Exu2
            }
        }
        return state;
    }

    @Nullable
    private static <T> T iterativeSearch(Collection<T> collection, Predicate<T> matchingFct) {
        for (T element : collection) {
            if(matchingFct.test(element)) {
                return element;
            }
        }
        return null;
    }

    public static void setAsSubTag(NBTTagCompound compound, String tag, Consumer<NBTTagCompound> applyFct) {
        NBTTagCompound newTag = new NBTTagCompound();
        applyFct.accept(newTag);
        compound.setTag(tag, newTag);
    }

    public static void writeBlockPosToNBT(BlockPos pos, NBTTagCompound compound) {
        compound.setInt("bposX", pos.getX());
        compound.setInt("bposY", pos.getY());
        compound.setInt("bposZ", pos.getZ());
    }

    public static BlockPos readBlockPosFromNBT(NBTTagCompound compound) {
        int x = compound.getInt("bposX");
        int y = compound.getInt("bposY");
        int z = compound.getInt("bposZ");
        return new BlockPos(x, y, z);
    }

}
