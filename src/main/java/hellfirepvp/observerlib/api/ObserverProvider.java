package hellfirepvp.observerlib.api;

import com.mojang.serialization.MapCodec;

import javax.annotation.Nonnull;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverProvider
 * Created by HellFirePvP
 * Date: 24.04.2019 / 17:47
 */
public abstract class ObserverProvider<T extends ChangeObserver<T>> {

    /**
     * Get the coded for the provided observers.
     *
     * @return the codec
     */
    public abstract MapCodec<T> codec();

    /**
     * Provides a new observer of the current provider.
     *
     * @return a new observer
     */
    @Nonnull
    public abstract T newObserver();
}
