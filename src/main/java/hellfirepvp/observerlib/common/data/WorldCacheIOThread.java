package hellfirepvp.observerlib.common.data;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.serialization.Codec;
import hellfirepvp.observerlib.ObserverLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.SharedConstants;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

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

    private final Map<WorldCacheDomain, Map<ResourceLocation, List<IWorldRelatedData<?>>>> worldSaveQueue = Maps.newHashMap();
    private final Map<WorldCacheDomain, Map<ResourceLocation, List<IWorldRelatedData<?>>>> awaitingSaveQueue = Maps.newHashMap();
    private boolean inSave = false, skipTick = false;

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
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData<?>>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        awaitingSaveQueue.clear();
        inSave = false;
    }

    private void flushAndSaveAll() {
        skipTick = true;
        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData<?>>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        saveAllNow();

        worldSaveQueue.clear();
        awaitingSaveQueue.clear();

        skipTick = false;
        inSave = false;
    }

    static void scheduleSave(WorldCacheDomain domain, ResourceLocation dimTypeName, IWorldRelatedData<?> worldRelatedData) {
        WorldCacheIOThread tr = saveTask;
        if (saveTask == null) { //Server startup didn't finish
            return;
        }
        if (tr.inSave) {
            tr.awaitingSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimTypeName, id -> new ArrayList<>())
                    .add(worldRelatedData);
        } else {
            tr.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimTypeName, id -> new ArrayList<>())
                    .add(worldRelatedData);
        }
    }

    @Nonnull
    static <T extends IWorldRelatedData<T>> T loadNow(WorldCacheDomain domain, Level world, WorldCacheDomain.SaveKey<T> key) {
        T loaded = loadDataFromFile(domain, world.dimension().location(), key);
        loaded.onLoad(world);
        return loaded;
    }

    private void saveAllNow() {
        for (WorldCacheDomain domain : this.worldSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData<?>>> entry : this.worldSaveQueue.get(domain).entrySet()) {
                entry.getValue().forEach(data -> saveNow(domain, entry.getKey(), data));
            }
        }
    }

    private void saveNow(WorldCacheDomain domain, ResourceLocation dimTypeName, IWorldRelatedData<?> data) {
        try {
            saveDataToFile(domain.getSaveDirectory(), dimTypeName, data);
        } catch (IOException e) {
            ObserverLib.log.warn("Unable to save WorldData!");
            ObserverLib.log.warn("Affected data: Dim=" + dimTypeName + " key=" + data.getSaveKey().getIdentifier());
            ObserverLib.log.warn("Printing StackTrace details...");
            e.printStackTrace();
        }
        data.markSaved();
    }

    private static void saveDataToFile(File baseDirectory, ResourceLocation dimTypeName, IWorldRelatedData<?> data) throws IOException {
        DirectorySet f = getDirectorySet(baseDirectory, dimTypeName, data.getSaveKey());
        f.ensureDirectoriesExist();

        writeDataToFile(data, f);
        data.writeAdditionalData(f.getActualDirectory(), f.getBackupDirectory());
    }

    private static <T extends IWorldRelatedData<T>> void writeDataToFile(IWorldRelatedData<T> data, DirectorySet f) throws IOException {
        WorldCacheDomain.SaveKey<T> saveKey = data.getSaveKey();
        File saveFile = saveKey.createAndBackupSaveFile(f.getActualDirectory(), f.getBackupDirectory());

        Codec<T> dataCodec = data.getSaveKey().getInstanceCodec();
        Tag dataNbt = dataCodec.encodeStart(NbtOps.INSTANCE, (T) data).getOrThrow();

        CompoundTag dataTag = new CompoundTag();
        dataTag.put("data", dataNbt);
        NbtIo.write(dataTag, saveFile.toPath());
    }

    @Nonnull
    private static <T extends IWorldRelatedData<T>> T loadDataFromFile(WorldCacheDomain domain, ResourceLocation dimTypeName, WorldCacheDomain.SaveKey<T> key) {
        DirectorySet f = getDirectorySet(domain.getSaveDirectory(), dimTypeName, key);
        IWorldRelatedData.FileLoader<T> loader = createLoadingContext(f, key);

        ObserverLib.log.info("Loading WorldData {}/{} for level {}", key.getDomainName().getNamespace(), key.getIdentifier(), dimTypeName);
        T loaded = loader.loadData(key.saveFileResolver(), key.getInstanceCodec())
                .map(dataTpl -> {
                    dataTpl.getA().readAdditionalData(dataTpl.getB().getParentFile(), loader);
                    return dataTpl.getA();
                })
                .orElseGet(key::newInstance);
        ObserverLib.log.info("Loading WorldData {}/{} for level {} finished", key.getDomainName().getNamespace(), key.getIdentifier(), dimTypeName);
        return loaded;
    }

    private static <F> IWorldRelatedData.FileLoader<F> createLoadingContext(DirectorySet dirSet, WorldCacheDomain.SaveKey<?> rootKey) {
        String rootName = rootKey.saveFileResolver().resolveFile(new File("/")).getAbsolutePath();
        return (fileResolver, codec) -> {
            if (!dirSet.getActualDirectory().exists() && !dirSet.getBackupDirectory().exists()) {
                return Optional.empty();
            }
            // Resolve from root to get an idea of what's being loaded.
            String attemptName = fileResolver.resolveFile(new File("/")).getAbsolutePath();
            boolean isSaveRoot = attemptName.equals(rootName);

            F data = null;
            File dataFile = null;

            // Try load from actual fileset
            try {
                if (dirSet.getActualDirectory().exists()) {
                    dataFile = fileResolver.resolveFile(dirSet.getActualDirectory());
                    if (dataFile.exists()) {
                        CompoundTag dataTag = NbtIo.read(dataFile.toPath());
                        data = codec.parse(NbtOps.INSTANCE, dataTag.get("data")).getOrThrow(IOException::new);
                    }
                }
            } catch (Exception exc) {
                ObserverLib.log.warn("Loading level data {} failed for its actual save. Attempting load from backup.",
                        (isSaveRoot ? rootKey.getIdentifier() : rootKey.getIdentifier() + attemptName));
            }
            // Try load from backup fileset
            if (data == null) {
                try {
                    if (dirSet.getBackupDirectory().exists()) {
                        dataFile = fileResolver.resolveFile(dirSet.getBackupDirectory());
                        if (dataFile.exists()) {
                            CompoundTag dataTag = NbtIo.read(dataFile.toPath());
                            data = codec.parse(NbtOps.INSTANCE, dataTag.get("data")).getOrThrow(IOException::new);
                        }
                    }
                } catch (Exception exc) {
                    ObserverLib.log.warn("Loading level data {} failed for its backup save. Copying erroneous files to error directory.",
                            (isSaveRoot ? rootKey.getIdentifier() : rootKey.getIdentifier() + attemptName));
                }
            }
            // Copy files to error directory and give up.
            if (data == null) {
                DirectorySet errorSet = dirSet.getErrorDirectories();
                try {
                    if (dirSet.getActualDirectory().exists()) {
                        Files.copy(dirSet.getActualDirectory(), errorSet.getActualDirectory());
                    }
                    if (dirSet.getBackupDirectory().exists()) {
                        Files.copy(dirSet.getBackupDirectory(), errorSet.getBackupDirectory());
                    }
                } catch (Exception e) {
                    ObserverLib.log.warn("Copying erroneous level data {} to the error directory failed.",
                            (isSaveRoot ? rootKey.getIdentifier() : rootKey.getIdentifier() + attemptName));
                    ObserverLib.log.error("Copying files failed.", e);
                }
            }
            if (data == null) {
                return Optional.empty();
            }
            return Optional.of(new Tuple<>(data, dataFile));
        };
    }

    private synchronized static DirectorySet getDirectorySet(File baseDirectory, ResourceLocation dimTypeName, WorldCacheDomain.SaveKey<?> key) {
        File worldDir = new File(baseDirectory, "DIM_" + sanitizeFileName(dimTypeName.toString()));
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        } else {
            ensureFolder(worldDir);
        }
        return new DirectorySet(new File(worldDir, key.getIdentifier()));
    }

    private static void ensureFolder(File f) {
        if (!f.isDirectory()) {
            ObserverLib.log.warn("dataFile exists, but is a file instead of a folder! Please ensure that this is a folder/delete the file!");
            ObserverLib.log.warn("Encountered illegal state. Crashing to prevent further, harder to resolve errors!");
            throw new IllegalStateException("Affected file: " + f.getAbsolutePath());
        }
    }

    private static String sanitizeFileName(String name) {
        name = name.trim().replace(' ', '_').toLowerCase();
        for (char c0 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            name = name.replace(c0, '_');
        }
        name = name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"); //Anything else that falls through
        return name;
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

        void ensureDirectoriesExist() {
            if (!this.getParentDirectory().exists()) this.getParentDirectory().mkdirs();
            if (!this.getActualDirectory().exists()) this.getActualDirectory().mkdirs();
            if (!this.getBackupDirectory().exists()) this.getBackupDirectory().mkdirs();
        }
    }
}
