package hellfirepvp.observerlib.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.Property;
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
    public static CompoundNBT getBlockStateNBTTag(BlockState state) {
        if (state.getBlock().getRegistryName() == null) {
            state = Blocks.AIR.getDefaultState();
        }
        CompoundNBT tag = new CompoundNBT();
        tag.putString("registryName", state.getBlock().getRegistryName().toString());
        ListNBT properties = new ListNBT();
        for (Property property : state.getProperties()) {
            CompoundNBT propTag = new CompoundNBT();
            try {
                propTag.putString("value", property.getName(state.get(property)));
            } catch (Exception exc) {
                continue;
            }
            propTag.putString("property", property.getName());
            properties.add(propTag);
        }
        tag.put("properties", properties);
        return tag;
    }

    @Nullable
    public static <T extends Comparable<T>> BlockState getBlockStateFromTag(CompoundNBT cmp, BlockState _default) {
        ResourceLocation key = new ResourceLocation(cmp.getString("registryName"));
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if(block == null || block == Blocks.AIR) return _default;
        BlockState state = block.getDefaultState();
        Collection<Property<?>> properties = state.getProperties();
        ListNBT list = cmp.getList("properties", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT propertyTag = list.getCompound(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            Property<T> match = (Property<T>) iterativeSearch(properties, prop -> prop.getName().equalsIgnoreCase(propertyStr));
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

    public static void setAsSubTag(CompoundNBT compound, String tag, Consumer<CompoundNBT> applyFct) {
        CompoundNBT newTag = new CompoundNBT();
        applyFct.accept(newTag);
        compound.put(tag, newTag);
    }

    public static void writeBlockPosToNBT(BlockPos pos, CompoundNBT compound) {
        compound.putInt("bposX", pos.getX());
        compound.putInt("bposY", pos.getY());
        compound.putInt("bposZ", pos.getZ());
    }

    public static BlockPos readBlockPosFromNBT(CompoundNBT compound) {
        int x = compound.getInt("bposX");
        int y = compound.getInt("bposY");
        int z = compound.getInt("bposZ");
        return new BlockPos(x, y, z);
    }

}
