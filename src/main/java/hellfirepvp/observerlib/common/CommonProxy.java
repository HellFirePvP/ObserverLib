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
import hellfirepvp.observerlib.common.registry.RegistryStructures;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import hellfirepvp.observerlib.common.util.tick.TickManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.NewRegistryEvent;

import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:25
 */
public class CommonProxy {

    private TickManager tickManager;

    public void initialize() {
        this.tickManager = new TickManager();
        this.attachTickListeners(this.tickManager::register);

        BlockChangeNotifier.addListener(new StructureIntegrityObserver());
    }

    public void attachLifecycle(IEventBus modEventBus) {
        modEventBus.addGenericListener(Block.class, this::registerBlock);

        modEventBus.addListener(this::registerRegistries);
    }

    private void registerBlock(RegistryEvent.Register<Block> event) {
        ObserverHelper.blockAirRequirement = new BlockAirRequirement();
        ObserverHelper.blockAirRequirement.setRegistryName(new ResourceLocation(ObserverLib.MODID, "air_preview"));
        event.getRegistry().register(ObserverHelper.blockAirRequirement);
    }

    private void registerRegistries(NewRegistryEvent event) {
        RegistryProviders.initialize(event);
        RegistryStructures.initialize(event);
    }

    public void attachEventHandlers(IEventBus eventBus) {
        eventBus.addListener(this::onServerStarted);
        eventBus.addListener(this::onServerStopping);

        EventHandlerIO.init(eventBus);
        this.tickManager.attachListeners(eventBus);
    }

    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(WorldCacheManager.getInstance());
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
