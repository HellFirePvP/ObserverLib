package hellfirepvp.observerlib.common;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.common.block.BlockAirRequirement;
import hellfirepvp.observerlib.common.change.StructureIntegrityObserver;
import hellfirepvp.observerlib.common.data.WorldCacheIOThread;
import hellfirepvp.observerlib.common.data.WorldCacheManager;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import hellfirepvp.observerlib.common.event.handler.EventHandlerIO;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:25
 */
public class CommonProxy {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ObserverLib.MODID);

    public void initialize() {
        BlockChangeNotifier.addListener(new StructureIntegrityObserver());
    }

    public void attachLifecycle(IEventBus modEventBus) {
        this.registerBlocks(modEventBus);
        modEventBus.addListener(this::registerRegistries);
    }

    private void registerBlocks(IEventBus modEventBus) {
        ObserverHelper.blockAirRequirement = BLOCKS.register("air_preview", BlockAirRequirement::new);
        BLOCKS.register(modEventBus);
    }

    private void registerRegistries(NewRegistryEvent event) {
        RegistryProviders.initialize(event);
    }

    public void attachEventHandlers(IEventBus eventBus) {
        eventBus.addListener(this::onServerStarted);
        eventBus.addListener(this::onServerStopping);

        EventHandlerIO.init(eventBus);
    }

    private void onServerStarted(ServerStartedEvent event) {
        WorldCacheIOThread.onServerStart();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        WorldCacheManager.scheduleSaveAll();
        WorldCacheIOThread.onServerStop();
        WorldCacheManager.cleanUp();
    }

}
