package hellfirepvp.observerlib.common.block;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;

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

    public static boolean displayRequiredAir = false;

    public BlockAirRequirement() {
        super(Properties.of(Material.AIR)
                .noCollission()
                .noDrops()
                .randomTicks());
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {}

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rand) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return null;
    }
}
