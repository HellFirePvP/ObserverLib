package hellfirepvp.observerlib.common.data;

import com.google.common.io.Files;
import hellfirepvp.observerlib.ObserverLib;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatcherDataManager
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:46
 */
public class MatcherDataManager {

    private static MatcherDataManager instance = new MatcherDataManager();
    private static Map<Integer, StructureMatchingBuffer> cachedData = new HashMap<>();
    private static File saveDir;

    private MatcherDataManager() {}

    public static MatcherDataManager getInstance() {
        return instance;
    }

    public static void wipeCache() {
        cachedData.clear();
        saveDir = null;
    }

    public static StructureMatchingBuffer getOrLoadData(IWorld world) {
        StructureMatchingBuffer data = getFromCache(world);
        if(data != null) return data;
        return loadAndCache(world);
    }

    private synchronized static DataFileSet getDataFile(IWorld world) {
        if(world.isRemote())
            throw new IllegalArgumentException("Tried to access data change on clientside. This is a severe implementation error!");
        if(saveDir == null) {
            saveDir = new File(world.getSaveHandler().getWorldDirectory(), "ObserverLibData");
            if(!saveDir.exists()) {
                saveDir.mkdirs();
            } else {
                ensureFolder(saveDir);
            }
        }
        File worldDir = new File(saveDir, "DIM_" + world.getDimension().getType().getId());
        if(!worldDir.exists()) {
            worldDir.mkdirs();
        } else {
            ensureFolder(worldDir);
        }
        return new DataFileSet(new File(worldDir, "match.dat"));
    }

    private static void ensureFolder(File f) {
        if(!f.isDirectory()) {
            ObserverLib.log.warn("Structure matcher datafile exists, but is a file instead of a folder!");
            throw new IllegalStateException("Affected file: " + f.getAbsolutePath());
        }
    }

    @Nullable
    private static StructureMatchingBuffer getFromCache(IWorld world) {
        if(!cachedData.containsKey(world.getDimension().getType().getId())) return null;
        return cachedData.get(world.getDimension().getType().getId());
    }

    private static StructureMatchingBuffer loadAndCache(IWorld world) {
        StructureMatchingBuffer data = getFromCache(world);
        if(data != null) return data;

        int dimId = world.getDimension().getType().getId();
        StructureMatchingBuffer loaded = loadDataFromFile(world);


        StructureMatchingBuffer existing = cachedData.get(dimId);
        if (existing != null) {
            ObserverLib.log.warn("Duplicate loading of the change matcher data! Discarding old data.");
            ObserverLib.log.warn("Affected data: Dim=" + dimId);
            cachedData.remove(dimId);
        }
        cachedData.put(dimId, loaded);
        return loaded;
    }

    private static StructureMatchingBuffer loadDataFromFile(IWorld world) {
        DataFileSet f = getDataFile(world);
        if (!f.actualFile.exists() && !f.backupFile.exists()) {
            return new StructureMatchingBuffer();
        }
        ObserverLib.log.info("Load change matcher data for world " + world.getDimension().getType().getId());
        boolean errored = false;
        StructureMatchingBuffer data = null;
        try {
            if(f.actualFile.exists()) {
                data = attemptLoad(f.actualFile);
            }
        } catch (Exception exc) {
            ObserverLib.log.info("Loading change matcher data failed for its actual save file. Attempting load from backup file.");
            errored = true;
        }
        if(data == null) {
            try {
                if(f.backupFile.exists()) {
                    data = attemptLoad(f.backupFile);
                }
            } catch (Exception exc) {
                ObserverLib.log.info("Loading change matcher data failed for its backup save file. Creating empty one for current runtime and copying erroneous files to error files.");
                errored = true;
            }
        }
        if(data == null && errored) {
            DataFileSet errorSet = f.getErrorFileSet();
            try {
                if(f.actualFile.exists()) {
                    Files.copy(f.actualFile, errorSet.actualFile);
                    f.actualFile.delete();
                }
                if(f.backupFile.exists()) {
                    Files.copy(f.backupFile, errorSet.backupFile);
                    f.backupFile.delete();
                }
            } catch (Exception e) {
                ObserverLib.log.info("Attempting to copy erroneous change matcher data to its error files failed.");
                e.printStackTrace();
            }
        }
        if(data == null) {
            if(errored) {
                DataFileSet errorSet = f.getErrorFileSet();
                try {
                    if(f.actualFile.exists()) {
                        Files.copy(f.actualFile, errorSet.actualFile);
                        f.actualFile.delete();
                    }
                    if(f.backupFile.exists()) {
                        Files.copy(f.backupFile, errorSet.backupFile);
                        f.backupFile.delete();
                    }
                } catch (Exception e) {
                    ObserverLib.log.info("Attempting to copy erroneous change matcher data to its error files failed.");
                    e.printStackTrace();
                }
            }
            data = new StructureMatchingBuffer();
        }
        ObserverLib.log.info("Loading of change matcher data for world " + world.getDimension().getType().getId() + " finished.");
        return data;
    }

    private static StructureMatchingBuffer attemptLoad(File f) throws IOException {
        StructureMatchingBuffer data = new StructureMatchingBuffer();
        NBTTagCompound cmp = CompressedStreamTools.read(f);
        data.readFromNBT(cmp);
        return data;
    }

    private static void saveDataToFile(IWorld world, StructureMatchingBuffer data) throws IOException {
        DataFileSet f = getDataFile(world);
        if(!f.actualFile.getParentFile().exists()) {
            f.actualFile.getParentFile().mkdirs();
        }
        if(f.actualFile.exists()) {
            try {
                Files.copy(f.actualFile, f.backupFile);
            } catch (Exception exc) {
                ObserverLib.log.info("Copying change matcher's actual file to its backup file failed!");
                exc.printStackTrace();
            }
        }
        if (!f.actualFile.exists()) {
            f.actualFile.createNewFile();
        }
        NBTTagCompound tag = new NBTTagCompound();
        data.writeToNBT(tag);
        CompressedStreamTools.write(tag, f.actualFile);
    }

    public void doSave(IWorld world) {
        int dimId = world.getDimension().getType().getId();
        StructureMatchingBuffer data = cachedData.get(dimId);
        if(data == null) {
            return;
        }
        if(data.needsSaving()) {
            try {
                saveDataToFile(world, data);
            } catch (IOException e) {
                ObserverLib.log.warn("Unable to save Structure matcher data!");
                ObserverLib.log.warn("Affected data: Dim=" + dimId);
                ObserverLib.log.warn("Printing StackTrace details...");
                e.printStackTrace();
            }
            data.clearDirtyFlag();
        }
    }

    private static class DataFileSet {

        private final File actualFile;
        private final File backupFile;

        private DataFileSet(File actualFile) {
            this.actualFile = actualFile;
            this.backupFile = new File(actualFile.getParent(), actualFile.getName() + ".back");
        }

        private DataFileSet getErrorFileSet() {
            return new DataFileSet(new File(actualFile.getParent(), actualFile.getName() + ".error"));
        }

    }

}
