package hellfirepvp.observerlib.common;

import hellfirepvp.observerlib.common.change.StructureIntegrityObserver;
import hellfirepvp.observerlib.common.data.io.WorldCacheIOManager;
import hellfirepvp.observerlib.common.data.io.WorldCacheSaveThread;
import hellfirepvp.observerlib.common.data.WorldCacheManager;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import hellfirepvp.observerlib.common.event.handler.EventHandlerIO;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.registry.RegistryStructures;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import hellfirepvp.observerlib.common.util.tick.TickManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:25
 */
public class CommonProxy {

    private CommonScheduler commonScheduler;
    private TickManager tickManager;

    public void initialize() {
        this.commonScheduler = new CommonScheduler();

        this.tickManager = new TickManager();
        this.attachTickListeners(this.tickManager::register);

        BlockChangeNotifier.addListener(new StructureIntegrityObserver());

        RegistryProviders.initialize();
        RegistryStructures.initialize();
    }

    public void attachLifecycle(IEventBus modEventBus) {

    }

    public void attachEventHandlers(IEventBus eventBus) {
        eventBus.addListener(this::onServerStarted);
        eventBus.addListener(this::onServerStopping);

        EventHandlerIO.init(eventBus);
        this.tickManager.attachListeners(eventBus);
    }

    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(this.commonScheduler);
        registrar.accept(WorldCacheManager.getInstance());
    }

    private void onServerStarted(FMLServerStartedEvent event) {
        WorldCacheIOManager.onServerStart();
    }

    private void onServerStopping(FMLServerStoppingEvent event) {
        WorldCacheIOManager.onServerStop();
        WorldCacheManager.cleanUp();
    }

    public void scheduleDelayed(Runnable r, int tickDelay) {
        this.commonScheduler.addRunnable(r, tickDelay);
    }

    public void scheduleDelayed(Runnable r) {
        this.scheduleDelayed(r, 0);
    }
}
