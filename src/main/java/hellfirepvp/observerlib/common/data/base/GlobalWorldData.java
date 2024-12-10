package hellfirepvp.observerlib.common.data.base;

import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: GlobalWorldData
 * Created by HellFirePvP
 * Date: 29.05.2019 / 21:56
 */
public abstract class GlobalWorldData<T extends GlobalWorldData<T>> extends CachedWorldData<T> {

    private boolean dirty = false;

    protected GlobalWorldData(WorldCacheDomain.SaveKey<T> key) {
        super(key);
    }

    public final void markDirty() {
        this.dirty = true;
    }

    public final boolean needsSaving() {
        return this.dirty;
    }

    public final void markSaved() {
        this.dirty = false;
    }
}
