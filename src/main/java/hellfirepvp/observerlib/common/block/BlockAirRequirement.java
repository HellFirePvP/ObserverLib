package hellfirepvp.observerlib.common.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
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
        super(Block.Properties.copy(Blocks.AIR)
                .noCollission()
                .noLootTable()
                .randomTicks());
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
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