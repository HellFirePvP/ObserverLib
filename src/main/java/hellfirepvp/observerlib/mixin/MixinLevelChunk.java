package hellfirepvp.observerlib.mixin;

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

    private BlockState prevState = null;

    @Shadow @Final Level level;

    @Shadow public abstract BlockState getBlockState(BlockPos p_62923_);

    @Inject(
            method = "setBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;",
                    ordinal = 0
            )
    )
    public void onBlockStateUpdate(BlockPos pos, BlockState newState, boolean arg2, CallbackInfoReturnable<BlockState> cir) {
        if (this.prevState == null || this.level.isClientSide() || this.prevState == newState) {
            return;
        }
        LevelChunk thisLevelChunk = (LevelChunk)(Object) this;
        BlockChangeNotifier.onBlockChange(this.level, thisLevelChunk, pos, this.prevState, newState);
        this.prevState = null;
    }

    @Inject(
            method = "setBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;",
                    shift = At.Shift.BEFORE
            )
    )
    public void preBlockStateUpdate(BlockPos pos, BlockState newState, boolean p_62867_, CallbackInfoReturnable<BlockState> cir) {
        if (this.level.isClientSide()) {
            return;
        }
        this.prevState = this.getBlockState(pos);
    }

}
