package hellfirepvp.observerlib.common.data;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.io.DirectorySet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
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
    private final DirectorySet directory;

    protected CachedWorldData(WorldCacheDomain.SaveKey<?> key, DirectorySet directory) {
        this.key = key;
        this.directory = directory;
    }

    public abstract boolean needsSaving();

    public abstract void updateTick(World world);

    @Override
    public final DirectorySet getDirectory() {
        return directory;
    }

    @Override
    public final WorldCacheDomain.SaveKey<?> getSaveKey() {
        return key;
    }

    public void onLoad(IWorld world) {}

    @Nonnull
    public static <T extends CachedWorldData> T loadWorldData(WorldCacheDomain.SaveKey<T> key, DirectorySet files) {
        if (!files.getActualDirectory().exists() && !files.getBackupDirectory().exists()) {
            return key.getNewInstance(key, files);
        }
        boolean errored = false;
        T data = null;
        try {
            if (files.getActualDirectory().exists()) {
                data = attemptLoad(key, files);
            }
        } catch (Exception exc) {
            ObserverLib.log.info("Loading worlddata '" + key.getIdentifier() + "' failed for its actual save. Attempting load from backup.");
            errored = true;
        }
        if (data == null) {
            try {
                if (files.getBackupDirectory().exists()) {
                    overwriteDirectory(files.getBackupDirectory(), files.getActualDirectory());
                    data = attemptLoad(key, files);
                }
            } catch (Exception exc) {
                ObserverLib.log.info("Loading worlddata '" + key.getIdentifier() + "' failed for its backup save. Creating empty one for current backup and copying erroneous files to error directory.");
                errored = true;
            }
        }
        if (data == null && errored) {
            DirectorySet errorSet = files.getErrorDirectories();
            try {
                overwriteDirectory(files.getActualDirectory(true), errorSet.getActualDirectory(true));
                FileUtils.deleteDirectory(files.getActualDirectory());

                overwriteDirectory(files.getBackupDirectory(true), errorSet.getBackupDirectory(true));
                FileUtils.deleteDirectory(files.getBackupDirectory());
            } catch (Exception e) {
                ObserverLib.log.info("Attempting to copy erroneous worlddata '" + key.getIdentifier() + "' to its error files directory failed.");
                e.printStackTrace();
            }
        }
        if (data == null) {
            data = key.getNewInstance(key, files);
        }
        return data;
    }

    private static void overwriteDirectory(File srcDir, File dstDir) throws IOException {
        dstDir.delete();
        FileUtils.copyDirectory(srcDir, dstDir);
    }

    private static <T extends CachedWorldData> T attemptLoad(WorldCacheDomain.SaveKey<T> key, DirectorySet directory) throws IOException {
        T data = key.getNewInstance(key, directory);
        data.readData();
        return data;
    }

}
