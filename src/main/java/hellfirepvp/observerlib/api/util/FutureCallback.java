package hellfirepvp.observerlib.api.util;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: FutureCallback
 * Created by HellFirePvP
 * Date: 03.06.2020 / 18:29
 */
public interface FutureCallback<T> extends com.google.common.util.concurrent.FutureCallback<T> {

    @Override
    default void onFailure(Throwable t) {
        throw new RuntimeException(t);
    }
}
