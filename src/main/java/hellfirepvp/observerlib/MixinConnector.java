package hellfirepvp.observerlib;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinConnector
 * Created by HellFirePvP
 * Date: 27.05.2022 / 22:03
 */
public class MixinConnector implements IMixinConnector {

    @Override
    public void connect() {
        Mixins.addConfiguration(String.format("assets/%s/%s.mixins.json", ObserverLib.MODID, ObserverLib.MODID));
    }
}
