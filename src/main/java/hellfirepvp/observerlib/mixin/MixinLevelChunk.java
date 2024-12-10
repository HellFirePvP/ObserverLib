package hellfirepvp.observerlib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on Github.
 * Class: MixinLevelChunk
 * Created by HellFirePvP
 * Date: 06.06.2022 / 09:26
 */
@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {

    @Inject(
            method = "setBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"
            )
    )
    public void onBlockStateUpdate(BlockPos pos, BlockState newState, boolean isMoving, CallbackInfoReturnable<BlockState> cir, @Local(ordinal = 1) BlockState oldState) {
        LevelChunk thisLevelChunk = (LevelChunk)(Object) this;
        Level level = thisLevelChunk.getLevel();
        if (level.isClientSide()) {
            return;
        }
        BlockChangeNotifier.onBlockChange(level, thisLevelChunk, pos, oldState, newState);
    }
}
