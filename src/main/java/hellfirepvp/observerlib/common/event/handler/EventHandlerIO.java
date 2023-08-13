package hellfirepvp.observerlib.common.event.handler;

import hellfirepvp.observerlib.common.data.WorldCacheManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
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

    private static void onSave(LevelEvent.Save event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof Level)) {
            return;
        }
        WorldCacheManager.getInstance().doSave((Level) event.getLevel());
    }

}
