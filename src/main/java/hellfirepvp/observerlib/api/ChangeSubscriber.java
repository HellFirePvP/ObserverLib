package hellfirepvp.observerlib.api;

import net.minecraft.world.IWorld;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ChangeSubscriber
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:31
 */
public interface ChangeSubscriber<T extends ChangeObserver> {

    public T getObserver();

    public boolean matches(IWorld world);

}
