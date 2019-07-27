package hellfirepvp.observerlib.common.data;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import hellfirepvp.observerlib.ObserverLib;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheIOThread
 * Created by HellFirePvP
 * Date: 29.06.2019 / 19:50
 */
public class WorldCacheIOThread extends TimerTask {

    private static WorldCacheIOThread saveTask;
    private static Timer ioThread;

    private Map<WorldCacheDomain, Map<Integer, List<IWorldRelatedData>>> worldSaveQueue = Maps.newHashMap();
    private Map<WorldCacheDomain, Map<Integer, List<IWorldRelatedData>>> awaitingSaveQueue = Maps.newHashMap();
    private boolean inSave = false, skipTick = false;
    private static File saveDir;

    private WorldCacheIOThread() {}

    public static void onServerStart() {
        if (ioThread != null) {
            return;
        }
        saveTask = new WorldCacheIOThread();
        ioThread = new Timer("WorldCacheIOThread", true);
        ioThread.scheduleAtFixedRate(saveTask, 30_000, 30_000);
    }

    public static void onServerStop() {
        saveTask.flushAndSaveAll();

        saveTask.cancel();
        saveTask = null;
        ioThread.cancel();
        ioThread = null;
    }

    @Override
    public void run() {
        if (skipTick) {
            return;
        }

        inSave = true;
        saveAllNow();
        worldSaveQueue.clear();

        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<Integer, List<IWorldRelatedData>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        awaitingSaveQueue.clear();
        inSave = false;
    }

    private void flushAndSaveAll() {
        skipTick = true;
        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<Integer, List<IWorldRelatedData>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        saveAllNow();

        worldSaveQueue.clear();
        awaitingSaveQueue.clear();

        skipTick = false;
        inSave = false;
    }

    static void scheduleSave(WorldCacheDomain domain, int dimensionId, IWorldRelatedData worldRelatedData) {
        WorldCacheIOThread tr = saveTask;
        if (saveTask == null) { //Server startup didn't finish
            return;
        }
        if (tr.inSave) {
            tr.awaitingSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimensionId, id -> new ArrayList<>())
                    .add(worldRelatedData);
        } else {
            tr.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimensionId, id -> new ArrayList<>())
                    .add(worldRelatedData);
        }
    }

    @Nonnull
    static <T extends CachedWorldData> T loadNow(WorldCacheDomain domain, World world, WorldCacheDomain.SaveKey<T> key) {
        T loaded = loadDataFromFile(domain, world.getDimension().getType().getId(), key);
        loaded.onLoad(world);
        return loaded;
    }

    private void saveAllNow() {
        for (WorldCacheDomain domain : this.worldSaveQueue.keySet()) {
            for (Map.Entry<Integer, List<IWorldRelatedData>> entry : this.worldSaveQueue.get(domain).entrySet()) {
                entry.getValue().forEach(data -> saveNow(domain, entry.getKey(), data));
            }
        }
    }

    private void saveNow(WorldCacheDomain domain, int dimensionId, IWorldRelatedData data) {
        try {
            saveDataToFile(domain.getSaveDirectory(), dimensionId, data);
        } catch (IOException e) {
            ObserverLib.log.warn("Unable to save WorldData!");
            ObserverLib.log.warn("Affected data: Dim=" + dimensionId + " key=" + data.getSaveKey().getIdentifier());
            ObserverLib.log.warn("Printing StackTrace details...");
            e.printStackTrace();
        }
        data.markSaved();
    }

    private static void saveDataToFile(File baseDirectory, int dimensionId, IWorldRelatedData data) throws IOException {
        DirectorySet f = getDirectorySet(baseDirectory, dimensionId, data.getSaveKey());
        if (!f.getParentDirectory().exists()) {
            f.getParentDirectory().mkdirs();
        }
        data.writeData(f.getActualDirectory(), f.getBackupDirectory());
    }

    @Nonnull
    private static <T extends CachedWorldData> T loadDataFromFile(WorldCacheDomain domain, int dimensionId, WorldCacheDomain.SaveKey<T> key) {
        DirectorySet f = getDirectorySet(domain.getSaveDirectory(), dimensionId, key);
        if (!f.getActualDirectory().exists() && !f.getBackupDirectory().exists()) {
            return key.getNewInstance(key);
        }
        ObserverLib.log.info("Load CachedWorldData '" + key.getIdentifier() + "' for world " + dimensionId);
        boolean errored = false;
        T data = null;
        try {
            if (f.getActualDirectory().exists()) {
                data = attemptLoad(key, f.getActualDirectory());
            }
        } catch (Exception exc) {
            ObserverLib.log.info("Loading worlddata '" + key.getIdentifier() + "' failed for its actual save. Attempting load from backup.");
            errored = true;
        }
        if(data == null) {
            try {
                if (f.getBackupDirectory().exists()) {
                    data = attemptLoad(key, f.getBackupDirectory());
                }
            } catch (Exception exc) {
                ObserverLib.log.info("Loading worlddata '" + key.getIdentifier() + "' failed for its backup save. Creating empty one for current runtime and copying erroneous files to error directory.");
                errored = true;
            }
        }
        if(data == null && errored) {
            DirectorySet errorSet = f.getErrorDirectories();
            try {
                if(f.getActualDirectory().exists()) {
                    Files.copy(f.getActualDirectory(), errorSet.getActualDirectory());
                    FileUtils.deleteDirectory(f.getActualDirectory());
                }
                if(f.getBackupDirectory().exists()) {
                    Files.copy(f.getBackupDirectory(), errorSet.getBackupDirectory());
                    FileUtils.deleteDirectory(f.getBackupDirectory());
                }
            } catch (Exception e) {
                ObserverLib.log.info("Attempting to copy erroneous worlddata '" + key.getIdentifier() + "' to its error files directory failed.");
                e.printStackTrace();
            }
        }
        if (data == null) {
            data = key.getNewInstance(key);
        }
        ObserverLib.log.info("Loading of '" + key.getIdentifier() + "' for world " + dimensionId + " finished.");
        return data;
    }

    private static <T extends CachedWorldData> T attemptLoad(WorldCacheDomain.SaveKey<T> key, File baseDirectory) throws IOException {
        T data = key.getNewInstance(key);
        data.readData(baseDirectory);
        return data;
    }

    private synchronized static DirectorySet getDirectorySet(File baseDirectory, int dimId, WorldCacheDomain.SaveKey<?> key) {
        if (saveDir == null) {
            saveDir = getServerWorldDirectory(baseDirectory);
        }

        File worldDir = new File(saveDir, "DIM_" + dimId);
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        } else {
            ensureFolder(worldDir);
        }
        return new DirectorySet(new File(worldDir, key.getIdentifier()));
    }

    private static File getServerWorldDirectory(File baseDirectory) {
        File pDir = new File(baseDirectory, "worlddata");
        if (!pDir.exists()) {
            pDir.mkdirs();
        }
        ensureFolder(pDir);
        return pDir;
    }

    private static void ensureFolder(File f) {
        if (!f.isDirectory()) {
            ObserverLib.log.warn("dataFile exists, but is a file instead of a folder! Please ensure that this is a folder/delete the file!");
            ObserverLib.log.warn("Encountered illegal state. Crashing to prevent further, harder to resolve errors!");
            throw new IllegalStateException("Affected file: " + f.getAbsolutePath());
        }
    }

    private static class DirectorySet {

        private final File actualDirectory;
        private final File backupDirectory;

        private DirectorySet(File worldDirectory) {
            this.actualDirectory = worldDirectory;
            this.backupDirectory = new File(worldDirectory.getParent(), worldDirectory.getName() + "-Backup");
        }

        File getParentDirectory() {
            return getActualDirectory().getParentFile();
        }

        File getActualDirectory() {
            return actualDirectory;
        }

        File getBackupDirectory() {
            return backupDirectory;
        }

        DirectorySet getErrorDirectories() {
            File errorDirectory = new File(actualDirectory.getParent(), actualDirectory.getName() + "-Error");
            if (!errorDirectory.exists()) {
                errorDirectory.mkdirs();
            }
            return new DirectorySet(errorDirectory);
        }

    }
}
