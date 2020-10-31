package hellfirepvp.observerlib.client;

import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.client.preview.StructurePreviewHandler;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import hellfirepvp.observerlib.common.CommonProxy;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientProxy
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:25
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void attachLifecycle(IEventBus modEventBus) {
        super.attachLifecycle(modEventBus);
        modEventBus.addListener(this::onClientSetup);
    }

    @Override
    public void attachEventHandlers(IEventBus eventBus) {
        super.attachEventHandlers(eventBus);

        ClientTickHelper.INSTANCE.attachEventListener(eventBus);
        StructurePreviewHandler.getInstance().attachEventListeners(eventBus);
    }

    @Override
    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        super.attachTickListeners(registrar);

        StructurePreviewHandler.getInstance().attachTickHandlers(registrar);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ObserverHelper.blockAirRequirement, RenderType.getTranslucent());
    }
}
