package hellfirepvp.observerlib.mixin;

import hellfirepvp.observerlib.ObserverLib;
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

    @Shadow @Final Level level;

    @Inject(
            method = "setBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onBlockStateUpdate(BlockPos p_62865_, BlockState p_62866_, boolean p_62867_, CallbackInfoReturnable<BlockState> cir, int i, LevelChunkSection levelchunksection, boolean flag, int j, int k, int l, BlockState blockstate) {
        if (this.level.isClientSide() || this.level.captureBlockSnapshots) {
            return;
        }
        LevelChunk thisLevelChunk = (LevelChunk)(Object) this;
        BlockChangeNotifier.onBlockChange(this.level, thisLevelChunk, p_62865_, blockstate, p_62866_);
    }
}
