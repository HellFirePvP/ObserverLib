package hellfirepvp.observerlib.common.data.io;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheThread
 * Created by HellFirePvP
 * Date: 03.06.2020 / 17:08
 */
public abstract class WorldCacheThread extends TimerTask {

    private final Timer ioThread;

    protected WorldCacheThread() {
        this.ioThread = new Timer(this.getClass().getSimpleName(), true);
        this.ioThread.scheduleAtFixedRate(this, 30_000, 30_000);
    }

    @Override
    public boolean cancel() {
        this.ioThread.cancel();
        return super.cancel();
    }

    DirectorySet getDirectorySet(File baseDirectory, ResourceLocation dimKey, WorldCacheDomain.SaveKey<?> key) {
        String fileName = String.format("dim_%s_%s", dimKey.getPath(), dimKey.getNamespace());
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        File worldDir = new File(baseDirectory, fileName);
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        } else {
            ensureFolder(worldDir);
        }
        return new DirectorySet(new File(worldDir, key.getIdentifier()));
    }

    private void ensureFolder(File f) {
        if (!f.isDirectory()) {
            ObserverLib.log.warn("dataFile exists, but is a file instead of a folder! Please ensure that this is a folder/delete the file!");
            ObserverLib.log.warn("Encountered illegal state. Crashing to prevent further, harder to resolve errors!");
            throw new IllegalStateException("Affected file: " + f.getAbsolutePath());
        }
    }
}
