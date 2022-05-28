package hellfirepvp.observerlib.mixin;

import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinLevel
 * Created by HellFirePvP
 * Date: 27.05.2022 / 22:04
 */
@Mixin(Level.class)
public class MixinLevel {

    @Inject(method = "markAndNotifyBlock", at = @At("HEAD"), remap = false)
    public void onBlockChangeUpdate(BlockPos pos, LevelChunk levelchunk, BlockState oldState, BlockState newState, int p_46607_, int p_46608_, CallbackInfo ci) {
        Level level = (Level)(Object) this;
        BlockChangeNotifier.onBlockChange(level, levelchunk, pos, oldState, newState);
    }

}
