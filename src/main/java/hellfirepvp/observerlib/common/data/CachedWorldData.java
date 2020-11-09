package hellfirepvp.observerlib.common.data;

import net.minecraft.world.World;

import java.util.Random;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: CachedWorldData
 * Created by HellFirePvP
 * Date: 02.08.2016 / 23:21
 */
public abstract class CachedWorldData implements IWorldRelatedData {

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

}
