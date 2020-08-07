package hellfirepvp.observerlib.api.util;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Small utility interface that allows for centralized guesses as to what items the given structure might need to be built.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ContentSerializable
 * Created by HellFirePvP
 * Date: 22.08.2019 / 21:31
 */
public interface ContentSerializable {

    /**
     * Returns this structure's potentially required items.
     *
     * @param world the world the structure is to be tested in
     * @param player the player to view the structure as blocks
     * @return a list of non-null itemstacks potentially required for this structure.
     */
    default List<ItemStack> getAsStacks(IBlockReader world, PlayerEntity player) {
        List<ItemStack> out = new LinkedList<>();
        if (!(this instanceof BlockArray)) {
            return out;
        }

        BlockArray thisArray = (BlockArray) this;
        long tick = ClientTickHelper.getClientTick();
        for (Map.Entry<BlockPos, MatchableState> structureEntry : thisArray.getContents().entrySet()) {
            BlockPos pos = structureEntry.getKey();
            BlockState sample = structureEntry.getValue().getDescriptiveState(tick);

            ItemStack stack = ItemStack.EMPTY;
            if (!sample.getFluidState().isEmpty() && sample.getFluidState().isSource()) {
                Fluid f = sample.getFluidState().getFluid();
                stack = FluidUtil.getFilledBucket(new FluidStack(f, FluidAttributes.BUCKET_VOLUME));
            }

            if (stack.isEmpty()) {
                try {
                    stack = sample.getBlock().getPickBlock(sample,
                            new BlockRayTraceResult(Vector3d.ZERO, Direction.UP, pos, false), world, pos, player);
                } catch (Exception ignored) {}
            }

            if (!stack.isEmpty()) {
                ResourceLocation needle = stack.getItem().getRegistryName();
                ItemStack existing = ItemStack.EMPTY;
                for (ItemStack i : out) {
                    if (i.getItem().getRegistryName().equals(needle)) {
                        existing = i;
                        break;
                    }
                }
                if (existing.isEmpty()) {
                    out.add(stack);
                } else {
                    existing.setCount(existing.getCount() + stack.getCount());
                }
            }
        }
        return out;
    }

}
