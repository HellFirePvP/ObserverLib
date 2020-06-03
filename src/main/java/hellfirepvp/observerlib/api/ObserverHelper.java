package hellfirepvp.observerlib.api;

import hellfirepvp.observerlib.api.util.FutureCallback;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverHelper
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:19
 */
public abstract class ObserverHelper {

    //The current API instance.
    private static ObserverHelper helperInstance = null;

    /**
     * Retrieve the current API instance using this getter.
     */
    public static ObserverHelper getHelper() {
        return helperInstance;
    }

    /**
     * Internal. Setting API instance during mod-instance construction.
     */
    public static void setHelper(ObserverHelper helperInstance) {
        ObserverHelper.helperInstance = helperInstance;
    }

    /**
     * Start watching changes in the world using a specific observer provider.
     * Offset by the specified center position.
     *
     * Will remove an existing subscriber if there is already one at that world + position combination that is NOT
     * the same type of observer (determined by registry-name) trying to be set there.
     *
     * @param world the world watching in
     * @param center the offset the observer should watch at
     * @param provider the provider providing a new observer instance for the observation
     * @param <T> the type of observer the provider is expected to return for generic usage
     * @param callback a callback with the new subscriber watching the specified area OR the existing subscriber if one at the specified
     *      offset already exists in that world at that position
     */
    public abstract <T extends ChangeObserver> void observeArea(World world, BlockPos center, ObserverProvider provider, FutureCallback<ChangeSubscriber<T>> callback);

    /**
     * Removes an observer at the given world + position combination.
     *
     * @param world the world to remove the observer from
     * @param pos the position the observer is expected to be at
     * @param callback a callback containing true, if there was a observer at the world + position combination and it could be removed, false otherwise
     */
    public abstract void removeObserver(World world, BlockPos pos, FutureCallback<Boolean> callback);

    /**
     * Returns the current observation subscriber at the given world + position if there is one.
     *
     * @param world the world to retrieve the observation subscriber from
     * @param pos the position to check at
     * @param callback a callback containing the observation subscriber at that position or null if none is found there
     */
    public abstract void getSubscriber(World world, BlockPos pos, FutureCallback<ChangeSubscriber<? extends ChangeObserver>> callback);

}
