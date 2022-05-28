package hellfirepvp.observerlib.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderSystemUtil
 * Created by HellFirePvP
 * Date: 27.05.2022 / 23:04
 */
public class RenderSystemUtil {

    public static void enableAlphaTest() {
        RenderSystem.assertOnRenderThread();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void disableAlphaTest() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    public static void enableLighting() {
        RenderSystem.assertOnRenderThread();
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public static void disableLighting() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_LIGHTING);
    }

}
