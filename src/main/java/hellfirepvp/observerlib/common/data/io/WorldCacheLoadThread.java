package hellfirepvp.observerlib.common.data.io;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.base.SectionWorldData;
import hellfirepvp.observerlib.common.data.base.WorldSection;
import hellfirepvp.observerlib.common.util.AlternatingSet;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheLoadThread
 * Created by HellFirePvP
 * Date: 03.06.2020 / 16:55
 */
public class WorldCacheLoadThread extends WorldCacheThread {

    private AlternatingSet<LoadEntry> loadQueue = new AlternatingSet<>();

    @Override
    public void run() {
        try {
            loadQueue.forEach(entry -> {
                ObserverLib.getProxy().scheduleDelayed(entry.run);
                return false;
            });
        } catch (IOException ignored) {}
    }

    <T extends CachedWorldData> void queueWorldDataLoad(WorldCacheDomain domain, ResourceLocation dimKey, WorldCacheDomain.SaveKey<T> key, Consumer<T> onLoad) {
        this.queueLoad(new WorldDataLoad(domain.getName(), dimKey, key.getIdentifier(), () -> {
            ObserverLib.log.info("Loading WorldData '" + key.getIdentifier() + "' for world " + dimKey);
            onLoad.accept(CachedWorldData.loadWorldData(key, getDirectorySet(domain.getSaveDirectory(), dimKey, key)));
            ObserverLib.log.info("Loading of WorldData '" + key.getIdentifier() + "' for world " + dimKey + " finished.");
        }));
    }

    <T extends WorldSection> void queueSectionLoad(SectionWorldData<T> worldData, int sectionX, int sectionZ, Consumer<T> onLoad) {
        this.queueLoad(new SectionDataLoad(worldData.getSaveKey().getIdentifier(), sectionX, sectionZ,
                () -> onLoad.accept(worldData.loadWorldSection(sectionX, sectionZ))));
    }

    <T extends WorldSection> void loadSection(SectionWorldData<T> worldData, int sectionX, int sectionZ, Consumer<T> onLoad) {
        this.queueLoad(new SectionDataLoad(worldData.getSaveKey().getIdentifier(), sectionX, sectionZ,
                () -> onLoad.accept(worldData.loadWorldSection(sectionX, sectionZ))).loadInstantly());
    }

    private void queueLoad(LoadEntry entry) {
        if (entry.loadInstantly) {
            entry.run.run();
        } else {
            if (!this.loadQueue.contains(entry)) {
                this.loadQueue.add(entry);
            }
        }
    }

    void clearAll() {
        loadQueue.clear();
    }

    private static abstract class LoadEntry {

        private final Runnable run;
        private boolean loadInstantly = false;

        LoadEntry(Runnable run) {
            this.run = run;
        }

        public LoadEntry loadInstantly() {
            this.loadInstantly = true;
            return this;
        }
    }

    private static class WorldDataLoad extends LoadEntry {

        private final ResourceLocation domainName, dimensionKey;
        private final String saveKeyName;

        WorldDataLoad(ResourceLocation domainName, ResourceLocation dimensionKey, String saveKeyName, Runnable run) {
            super(run);
            this.domainName = domainName;
            this.dimensionKey = dimensionKey;
            this.saveKeyName = saveKeyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorldDataLoad that = (WorldDataLoad) o;
            return Objects.equals(domainName, that.domainName) &&
                    Objects.equals(dimensionKey, that.dimensionKey) &&
                    Objects.equals(saveKeyName, that.saveKeyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(domainName, dimensionKey, saveKeyName);
        }
    }

    private static class SectionDataLoad extends LoadEntry {

        private String saveKeyName;
        private int sectionX, sectionZ;

        SectionDataLoad(String saveKeyName, int sectionX, int sectionZ, Runnable run) {
            super(run);
            this.saveKeyName = saveKeyName;
            this.sectionX = sectionX;
            this.sectionZ = sectionZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SectionDataLoad that = (SectionDataLoad) o;
            return sectionX == that.sectionX &&
                    sectionZ == that.sectionZ &&
                    Objects.equals(saveKeyName, that.saveKeyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(saveKeyName, sectionX, sectionZ);
        }
    }
}
