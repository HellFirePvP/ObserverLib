package hellfirepvp.observerlib.common.data.base;

import com.google.common.io.Files;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;

import java.io.File;
import java.io.IOException;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: GlobalWorldData
 * Created by HellFirePvP
 * Date: 29.05.2019 / 21:56
 */
public abstract class GlobalWorldData extends CachedWorldData {

    private boolean dirty = false;
    private final String saveFileName;

    protected GlobalWorldData(WorldCacheDomain.SaveKey<?> key) {
        super(key);
        this.saveFileName = key.getIdentifier() + ".dat";
    }

    public final void markDirty() {
        this.dirty = true;
    }

    private File getSaveFile(File directory) {
        return directory.toPath().resolve(this.saveFileName).toFile();
    }

    @Override
    public final void writeData(File baseDirectory, File backupDirectory) throws IOException {
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }
        File saveFile = this.getSaveFile(baseDirectory);
        if (saveFile.exists()) {
            try {
                Files.copy(saveFile, this.getSaveFile(backupDirectory));
            } catch (Exception exc) {
                ObserverLib.log.info("Copying '" + getSaveKey().getIdentifier() + "' 's actual file to its backup file failed!");
                exc.printStackTrace();
            }
        } else {
            saveFile.createNewFile();
        }

        CompoundNBT data = new CompoundNBT();
        this.readIO(() -> this.writeToNBT(data));
        CompressedStreamTools.write(data, saveFile);
    }

    @Override
    public final void readData(File baseDirectory) throws IOException {
        this.writeIO(() -> this.readFromNBT(CompressedStreamTools.read(this.getSaveFile(baseDirectory))));
    }

    public abstract void writeToNBT(CompoundNBT tag);

    public abstract void readFromNBT(CompoundNBT tag);

    public final boolean needsSaving() {
        return this.dirty;
    }

    public final void markSaved() {
        this.dirty = false;
    }

}
