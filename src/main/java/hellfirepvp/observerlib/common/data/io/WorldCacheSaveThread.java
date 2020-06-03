package hellfirepvp.observerlib.common.data.io;

import com.google.common.collect.Maps;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.IWorldRelatedData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheSaveThread
 * Created by HellFirePvP
 * Date: 29.06.2019 / 19:50
 */
public class WorldCacheSaveThread extends WorldCacheThread {

    private Map<WorldCacheDomain, Map<ResourceLocation, List<IWorldRelatedData>>> worldSaveQueue = Maps.newHashMap();
    private Map<WorldCacheDomain, Map<ResourceLocation, List<IWorldRelatedData>>> awaitingSaveQueue = Maps.newHashMap();
    private boolean inSave = false, skipTick = false;

    @Override
    public void run() {
        if (skipTick) {
            return;
        }

        inSave = true;
        saveAllNow();
        worldSaveQueue.clear();

        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        awaitingSaveQueue.clear();
        inSave = false;
    }

    void flushAndSaveAll() {
        skipTick = true;
        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        saveAllNow();

        worldSaveQueue.clear();
        awaitingSaveQueue.clear();

        skipTick = false;
        inSave = false;
    }

    void scheduleSave(WorldCacheDomain domain, ResourceLocation dimKey, IWorldRelatedData worldRelatedData) {
        if (inSave) {
            awaitingSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimKey, id -> new ArrayList<>())
                    .add(worldRelatedData);
        } else {
            worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimKey, id -> new ArrayList<>())
                    .add(worldRelatedData);
        }
    }

    private void saveAllNow() {
        for (WorldCacheDomain domain : this.worldSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData>> entry : this.worldSaveQueue.get(domain).entrySet()) {
                entry.getValue().forEach(data -> saveNow(domain, entry.getKey(), data));
            }
        }
    }

    private void saveNow(WorldCacheDomain domain, ResourceLocation dimKey, IWorldRelatedData data) {
        try {
            saveDataToFile(domain.getSaveDirectory(), dimKey, data);
        } catch (IOException e) {
            ObserverLib.log.warn("Unable to save WorldData!");
            ObserverLib.log.warn("Affected data: Dim=" + dimKey + " key=" + data.getSaveKey().getIdentifier());
            ObserverLib.log.warn("Printing StackTrace details...");
            e.printStackTrace();
        }
        data.markSaved();
    }

    private void saveDataToFile(File baseDirectory, ResourceLocation dimKey, IWorldRelatedData data) throws IOException {
        DirectorySet f = getDirectorySet(baseDirectory, dimKey, data.getSaveKey());
        if (!f.getParentDirectory().exists()) {
            f.getParentDirectory().mkdirs();
        }
        data.writeData();
    }
}
