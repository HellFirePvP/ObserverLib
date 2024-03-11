package hellfirepvp.observerlib.common.util.tick;

import net.neoforged.neoforge.event.TickEvent;

import java.util.EnumSet;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ITickHandler
 * Created by HellFirePvP
 * Date: 04.08.2016 / 11:21
 */
public interface ITickHandler {

    public void tick(TickEvent.Type type, Object... context);

    /**
     * WORLD, context: world
     * SERVER, context:
     * CLIENT, context:
     * RENDER, context: pTicks
     * PLAYER, context: player, side
     */
    public EnumSet<TickEvent.Type> getHandledTypes();

    public boolean canFire(TickEvent.Phase phase);

    public String getName();

}
