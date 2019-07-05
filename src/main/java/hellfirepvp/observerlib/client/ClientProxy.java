package hellfirepvp.observerlib.client;

import hellfirepvp.observerlib.client.util.ClientTickHelper;
import hellfirepvp.observerlib.common.CommonProxy;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientProxy
 * Created by HellFirePvP
 * Date: 06.03.2019 / 21:25
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void attachEventHandlers(IEventBus eventBus) {
        super.attachEventHandlers(eventBus);

        ClientTickHelper.INSTANCE.attachEventListener(eventBus);
    }
}
