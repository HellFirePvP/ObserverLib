package hellfirepvp.observerlib.api.util;

import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

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
    default List<ItemStack> getAsStacks(LevelReader world, Player player) {
        List<ItemStack> out = new LinkedList<>();
        if (!(this instanceof BlockArray thisArray)) {
            return out;
        }

        long tick = ClientTickHelper.getClientTick();
        for (Map.Entry<BlockPos, MatchableState> structureEntry : thisArray.getContents().entrySet()) {
            BlockPos pos = structureEntry.getKey();
            BlockState sample = structureEntry.getValue().getDescriptiveState(tick);

            ItemStack stack = ItemStack.EMPTY;
            if (!sample.getFluidState().isEmpty() && sample.getFluidState().isSource()) {
                Fluid f = sample.getFluidState().getType();
                stack = FluidUtil.getFilledBucket(new FluidStack(f, FluidType.BUCKET_VOLUME));
            }

            if (stack.isEmpty()) {
                try {
                    stack = sample.getCloneItemStack(new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false),
                            world, pos, player);
                } catch (Exception ignored) {}
            }

            if (!stack.isEmpty()) {
                Item needle = stack.getItem();

                ItemStack existing = ItemStack.EMPTY;
                for (ItemStack i : out) {
                    if (i.getItem().equals(needle)) {
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
