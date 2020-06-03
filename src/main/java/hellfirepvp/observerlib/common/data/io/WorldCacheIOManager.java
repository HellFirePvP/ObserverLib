package hellfirepvp.observerlib.common.data.io;

import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.IWorldRelatedData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.WorldCacheManager;
import hellfirepvp.observerlib.common.data.base.SectionWorldData;
import hellfirepvp.observerlib.common.data.base.WorldSection;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheIOManager
 * Created by HellFirePvP
 * Date: 03.06.2020 / 16:55
 */
public class WorldCacheIOManager {

    private static WorldCacheSaveThread saveThread = null;
    private static WorldCacheLoadThread loadThread = null;

    public static void onServerStart() {
        if (saveThread == null) {
            saveThread = new WorldCacheSaveThread();
        }
        if (loadThread == null) {
            loadThread = new WorldCacheLoadThread();
        }
    }

    public static void onServerStop() {
        if (saveThread != null) {
            WorldCacheManager.scheduleSaveAll();

            saveThread.flushAndSaveAll();
            saveThread.cancel();
            saveThread = null;
        }
        if (loadThread != null) {
            loadThread.clearAll();
            loadThread.cancel();
            loadThread = null;
        }
    }

    public static <T extends CachedWorldData> void scheduleCacheLoad(WorldCacheDomain domain,
                                                                     ResourceLocation dimKey,
                                                                     WorldCacheDomain.SaveKey<T> saveKey,
                                                                     Consumer<T> onLoad) {
        if (loadThread != null) {
            loadThread.queueWorldDataLoad(domain, dimKey, saveKey, onLoad);
        }
    }

    public static <T extends WorldSection> void scheduleSectionLoad(SectionWorldData<T> worldData,
                                                                    int sectionX,
                                                                    int sectionZ,
                                                                    Consumer<T> onLoad) {
        if (loadThread != null) {
            loadThread.queueSectionLoad(worldData, sectionX, sectionZ, onLoad);
        }
    }

    public static <T extends WorldSection> void loadSectionNow(SectionWorldData<T> worldData,
                                                                    int sectionX,
                                                                    int sectionZ,
                                                                    Consumer<T> onLoad) {
        if (loadThread != null) {
            loadThread.loadSection(worldData, sectionX, sectionZ, onLoad);
        }
    }

    public static void scheduleSave(WorldCacheDomain domain, ResourceLocation dimKey, IWorldRelatedData worldRelatedData) {
        if (saveThread != null) {
            saveThread.scheduleSave(domain, dimKey, worldRelatedData);
        }
    }
}
