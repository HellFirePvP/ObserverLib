/*******************************************************************************
 * HellFirePvP / ObserverLib 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ObserverLib
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.observerlib;

import hellfirepvp.observerlib.common.CommonProxy;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverLib
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:18
 */
@Mod(modid = ObserverLib.MODID, name = ObserverLib.NAME, version = ObserverLib.VERSION,
        dependencies = "required-after:forge@[14.23.5.2781,)",
        certificateFingerprint = "a0f0b759d895c15ceb3e3bcb5f3c2db7c582edf0",
        acceptedMinecraftVersions = "[1.12, 1.13)"
)
public class ObserverLib {

    public static final String MODID = "observerlib";
    public static final String NAME = "ObserverLib";
    public static final String VERSION = "0.0.1";
    public static final String CLIENT_PROXY = "hellfirepvp.observerlib.client.ClientProxy";
    public static final String COMMON_PROXY = "hellfirepvp.observerlib.common.CommonProxy";

    private static boolean devEnvChache = false;

    @Mod.Instance(MODID)
    public static ObserverLib instance;

    public static Logger log = LogManager.getLogger(NAME);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = VERSION;
        devEnvChache = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) {
        //WorldCacheManager.wipeCache();
    }

    public static boolean isRunningInDevEnvironment() {
        return devEnvChache;
    }

}
