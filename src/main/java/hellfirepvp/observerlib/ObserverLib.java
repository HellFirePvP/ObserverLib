/*******************************************************************************
 * HellFirePvP / ObserverLib 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ObserverLib
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.observerlib;

import hellfirepvp.observerlib.client.ClientProxy;
import hellfirepvp.observerlib.common.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverLib
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:18
 */
@Mod(ObserverLib.MODID)
public class ObserverLib {

    public static final String MODID = "observerlib";
    public static final String NAME = "ObserverLib";

    public static final Logger log = LogManager.getLogger(NAME);
    private static ObserverLib instance;

    private ModContainer modContainer;
    private CommonProxy proxy;

    public ObserverLib() {
        instance = this;
        this.modContainer = ModList.get().getModContainerById(MODID).get();

        this.proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        this.proxy.initialize();
        this.proxy.attachLifecycle(FMLJavaModLoadingContext.get().getModEventBus());
        this.proxy.attachEventHandlers(MinecraftForge.EVENT_BUS);
    }

    public static CommonProxy getProxy() {
        return getInstance().proxy;
    }

    public static ModContainer getModContainer() {
        return getInstance().modContainer;
    }

    public static ObserverLib getInstance() {
        return instance;
    }

}
