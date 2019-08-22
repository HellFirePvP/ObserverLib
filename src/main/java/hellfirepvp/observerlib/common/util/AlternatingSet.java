package hellfirepvp.observerlib.common.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: AlternatingSet
 * Created by HellFirePvP
 * Date: 22.08.2019 / 19:22
 */
public class AlternatingSet<T> {

    private final Object flipLock = new Object();

    private Set<T> actualSet = new HashSet<>();
    private Set<T> flippedSet = new HashSet<>();
    private boolean accessible = true;

    public void add(T entry) {
        synchronized (flipLock) {
            if (this.accessible) {
                this.actualSet.add(entry);
            } else {
                this.flippedSet.add(entry);
            }
        }
    }

    public void forEach(IOPredicate<T> entryConsumer) throws IOException {
        synchronized (flipLock) {
            this.accessible = false;
            Set<T> set = new HashSet<>();
            for (T t : this.actualSet) {
                if (entryConsumer.testUnsafe(t)) {
                    set.add(t);
                }
            }
            this.actualSet = set;
            this.accessible = true;
            this.actualSet.addAll(this.flippedSet);
            this.flippedSet.clear();
        }
    }

    public void clear() {
        synchronized (flipLock) {
            this.actualSet.clear();
            this.flippedSet.clear();
        }
    }

    public int size() {
        return this.actualSet.size() + this.flippedSet.size();
    }

    public boolean isEmpty() {
        return this.actualSet.isEmpty() && this.flippedSet.isEmpty();
    }

    public boolean contains(T o) {
        synchronized (flipLock) {
            return this.actualSet.contains(o) || this.flippedSet.contains(o);
        }
    }

}
