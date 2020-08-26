package hellfirepvp.observerlib.common.event.handler;

import hellfirepvp.observerlib.common.data.WorldCacheManager;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHandlerIO
 * Created by HellFirePvP
 * Date: 03.07.2019 / 15:35
 */
public class EventHandlerIO {

    public static void init(IEventBus eventBus) {
        eventBus.addListener(EventHandlerIO::onSave);
    }

    private static void onSave(WorldEvent.Save event) {
        if (event.getWorld().isRemote() || !(event.getWorld() instanceof World)) {
            return;
        }
        WorldCacheManager.getInstance().doSave((World) event.getWorld());
    }

}
