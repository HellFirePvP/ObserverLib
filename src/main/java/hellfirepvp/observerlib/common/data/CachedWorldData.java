package hellfirepvp.observerlib.common.data;

import hellfirepvp.observerlib.common.util.IORunnable;
import net.minecraft.world.World;

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
public abstract class CachedWorldData implements IWorldRelatedData {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected final Random rand = new Random();
    private final WorldCacheDomain.SaveKey<?> key;

    protected CachedWorldData(WorldCacheDomain.SaveKey<?> key) {
        this.key = key;
    }

    public abstract boolean needsSaving();

    public abstract void updateTick(World world);

    public final WorldCacheDomain.SaveKey<?> getSaveKey() {
        return key;
    }

    public void onLoad(World world) {}

    public <T> T write(Supplier<T> fn) {
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

    public <T> T read(Supplier<T> fn) {
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

    private <T> T lock(Supplier<Lock> lock, Supplier<T> fn) {
        lock.get().lock();
        try {
            return fn.get();
        } finally {
            lock.get().unlock();
        }
    }
}
