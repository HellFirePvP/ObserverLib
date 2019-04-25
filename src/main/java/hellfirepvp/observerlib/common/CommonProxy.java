/*******************************************************************************
 * HellFirePvP / ObserverLib 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ObserverLib
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.observerlib.common;

import hellfirepvp.observerlib.common.change.StructureIntegrityObserver;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.registry.RegistryStructures;
import hellfirepvp.observerlib.common.world.WorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:25
 */
public class CommonProxy {

    public void initialize() {
        RegistryProviders.initialize();
        RegistryStructures.initialize();
    }

    public void attachLifecycle(IEventBus modEventBus) {

    }

    public void attachEventHandlers(IEventBus eventBus) {
        eventBus.addListener(this::attachWorldListener);

        new StructureIntegrityObserver(eventBus);
    }

    private void attachWorldListener(WorldEvent.Load event) {
        if (event.getWorld() instanceof World) {
            ((World) event.getWorld()).addEventListener(new WorldEventListener());
        }
    }

}
