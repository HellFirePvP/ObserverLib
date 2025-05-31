package hellfirepvp.observerlib.common.data;

import hellfirepvp.observerlib.common.util.IORunnable;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: CachedWorldData
 * Created by HellFirePvP
 * Date: 02.08.2016 / 23:21
 */
public abstract class CachedWorldData<T extends CachedWorldData<T>> implements IWorldRelatedData<T> {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final WorldCacheDomain.SaveKey<T> key;

    protected CachedWorldData(WorldCacheDomain.SaveKey<T> key) {
        this.key = key;
    }

    public abstract boolean needsSaving();

    public final WorldCacheDomain.SaveKey<T> getSaveKey() {
        return key;
    }

    public <V> V write(Supplier<V> fn) {
        return this.lock(this.rwLock::writeLock, fn);
    }

    public void write(Runnable run) {
        this.lock(this.rwLock::writeLock, () -> {
            run.run();
            return null;
        });
    }

    public void writeIO(IORunnable run) throws IOException {
        this.rwLock.writeLock().lock();
        try {
            run.run();
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public <V> V read(Supplier<V> fn) {
        return this.lock(this.rwLock::readLock, fn);
    }

    public void read(Runnable run) {
        this.lock(this.rwLock::readLock, () -> {
            run.run();
            return null;
        });
    }

    public void readIO(IORunnable run) throws IOException {
        this.rwLock.readLock().lock();
        try {
            run.run();
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    private <V> V lock(Supplier<Lock> lock, Supplier<V> fn) {
        lock.get().lock();
        try {
            return fn.get();
        } finally {
            lock.get().unlock();
        }
    }
}
