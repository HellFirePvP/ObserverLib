/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.observerlib.client.util;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: LightmapUtil
 * Created by HellFirePvP
 * Date: 27.05.2022 / 23:51
 */
public class LightmapUtil {

    public static int getPackedFullbrightCoords() {
        return 0xF000F0;
    }

    public static int getPackedLightCoords(int lightValue) {
        return getPackedLightCoords(lightValue, lightValue);
    }

    public static int getPackedLightCoords(int skyLight, int blockLight) {
        return skyLight << 20 | blockLight << 4;
    }

    public static int getPackedLightCoords(BlockAndTintGetter world, BlockPos at) {
        return LevelRenderer.getLightColor(world, at);
    }
}
