package hellfirepvp.observerlib.common.block;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAirRequirement
 * Created by HellFirePvP
 * Date: 31.10.2020 / 21:10
 */
public class BlockAirRequirement extends AirBlock {

    public BlockAirRequirement() {
        super(Properties.create(Material.AIR)
                .doesNotBlockMovement()
                .noDrops()
                .tickRandomly());
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {}

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public int tickRate(IWorldReader world) {
        return 1;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return null;
    }
}
