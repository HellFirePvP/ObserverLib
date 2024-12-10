package hellfirepvp.observerlib.client.util;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientTickHelper
 * Created by HellFirePvP
 * Date: 30.04.2019 / 22:58
 */
public class ClientTickHelper {

    public static final ClientTickHelper INSTANCE = new ClientTickHelper();

    private static long tick = 0;

    private ClientTickHelper() {}

    public void attachEventListener(IEventBus eventBus) {
        eventBus.addListener(this::tick);
    }

    public static long getClientTick() {
        return tick;
    }

    private void tick(ClientTickEvent.Post event) {
        tick++;
    }
}
