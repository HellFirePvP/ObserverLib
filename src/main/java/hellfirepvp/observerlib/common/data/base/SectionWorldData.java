package hellfirepvp.observerlib.common.data.base;

import com.google.common.io.Files;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.util.FutureCallback;
import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.io.DirectorySet;
import hellfirepvp.observerlib.common.data.io.WorldCacheIOManager;
import hellfirepvp.observerlib.common.util.AlternatingSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: SectionWorldData
 * Created by HellFirePvP
 * Date: 29.05.2019 / 21:55
 */
public abstract class SectionWorldData<T extends WorldSection> extends CachedWorldData {

    public static final int PRECISION_REGION = 9;
    public static final int PRECISION_AREA = 6;
    public static final int PRECISION_SECTION = 5;
    public static final int PRECISION_CHUNK = 4;

    private Map<SectionKey, T> sections = new HashMap<>();
    private final int precision;
    private boolean loadSectionsInstantly = false;

    private AlternatingSet<SectionKey> dirtySections = new AlternatingSet<>();
    private Set<SectionKey> removedSections = new HashSet<>();

    protected SectionWorldData(WorldCacheDomain.SaveKey<?> key, DirectorySet directory, int sectionPrecision) {
        super(key, directory);
        this.precision = sectionPrecision;
    }

    protected SectionWorldData<T> loadSectionsInstantly() {
        this.loadSectionsInstantly = true;
        return this;
    }

    public void markDirty(Vec3i absolute) {
        SectionKey key = SectionKey.resolve(absolute, this.precision);
        T section = getLoadedSection(key);
        if (section != null) {
            this.dirtySections.add(key);
        }
    }

    public void markDirty(T section) {
        this.dirtySections.add(SectionKey.from(section));
    }

    protected abstract T createNewSection(int sectionX, int sectionZ);

    public void getOrCreateSections(Vec3i absoluteMin, Vec3i absoluteMax, FutureCallback<Collection<T>> callback) {
        SectionKey lower = SectionKey.resolve(absoluteMin, this.precision);
        SectionKey higher = SectionKey.resolve(absoluteMax, this.precision);
        Collection<SectionKey> waitingSectionKeys = new HashSet<>();
        for (int xx = lower.x; xx <= higher.x; xx++) {
            for (int zz = lower.z; zz <= higher.z; zz++) {
                waitingSectionKeys.add(new SectionKey(xx, zz));
            }
        }
        Collection<T> foundSections = new HashSet<>();
        for (SectionKey key : waitingSectionKeys) {
            this.getOrCreateSection(key, (loaded) -> {
                synchronized (foundSections) {
                    foundSections.add(loaded);
                    if (foundSections.size() == waitingSectionKeys.size()) {
                        callback.onSuccess(foundSections);
                    }
                }
            });
        }
    }

    public void getOrCreateSection(Vec3i absolute, FutureCallback<T> callback) {
        getOrCreateSection(SectionKey.resolve(absolute, this.precision), callback);
    }

    private void getOrCreateSection(SectionKey key, FutureCallback<T> callback) {
        if (this.sections.containsKey(key)) {
            callback.onSuccess(this.sections.get(key));
        } else if (this.loadSectionsInstantly) {
            WorldCacheIOManager.loadSectionNow(this, key.x, key.z, (section) -> {
                this.sections.put(key, section);
                callback.onSuccess(section);
            });
        } else {
            WorldCacheIOManager.scheduleSectionLoad(this, key.x, key.z, (section) -> {
                this.sections.put(key, section);
                callback.onSuccess(section);
            });
        }
    }

    @Nullable
    public T getLoadedSection(Vec3i absolute) {
        return this.getLoadedSection(SectionKey.resolve(absolute, this.precision));
    }

    @Nullable
    private T getLoadedSection(SectionKey key) {
        return this.sections.get(key);
    }

    public void removeSection(T section) {
        SectionKey key = SectionKey.from(section);
        this.dirtySections.remove(key);
        this.sections.remove(key);
        this.removedSections.add(key);
    }

    public void removeSection(Vec3i absolute) {
        SectionKey key = SectionKey.resolve(absolute, this.precision);
        this.dirtySections.remove(key);
        this.sections.remove(key);
        this.removedSections.add(key);
    }

    @Nonnull
    public Collection<T> getLoadedSections() {
        return this.sections.values();
    }

    @Override
    public boolean needsSaving() {
        return !this.dirtySections.isEmpty();
    }

    @Override
    public void markSaved() {
        this.dirtySections.clear();
    }

    public abstract void writeToNBT(CompoundNBT nbt);

    public abstract void readFromNBT(CompoundNBT nbt);

    private File getSaveFile(File directory, T section) {
        return this.getSaveFile(directory, section.getSectionX(), section.getSectionZ());
    }

    private File getSaveFile(File directory, int sectionX, int sectionZ) {
        return new File(directory, this.getSectionFileName(sectionX, sectionZ));
    }

    private String getSectionFileName(int sectionX, int sectionZ) {
        return String.format("%s_%s_%s.dat", this.getSaveKey().getIdentifier(), sectionX, sectionZ);
    }

    @Override
    public final void writeData() throws IOException {
        File generalSaveFile = new File(this.getDirectory().getActualDirectory(), "general.dat");
        if (generalSaveFile.exists()) {
            try {
                Files.copy(generalSaveFile, new File(this.getDirectory().getBackupDirectory(true), "general.dat"));
            } catch (Exception exc) {
                ObserverLib.log.info("Copying '" + getSaveKey().getIdentifier() + "' general actual file to its backup file failed!");
                exc.printStackTrace();
            }
        } else {
            generalSaveFile.createNewFile();
        }
        CompoundNBT generalData = new CompoundNBT();
        this.writeToNBT(generalData);
        CompressedStreamTools.write(generalData, generalSaveFile);

        this.dirtySections.forEach(key -> {
            T section = getLoadedSection(key);
            if (section != null) {

                File saveFile = this.getSaveFile(this.getDirectory().getActualDirectory(true), section);
                if (saveFile.exists()) {
                    try {
                        Files.copy(saveFile, this.getSaveFile(this.getDirectory().getBackupDirectory(true), section));
                    } catch (Exception exc) {
                        ObserverLib.log.info("Copying '" + getSaveKey().getIdentifier() + "' actual file to its backup file failed!");
                        exc.printStackTrace();
                    }
                } else {
                    saveFile.createNewFile();
                }

                CompoundNBT data = new CompoundNBT();
                section.writeToNBT(data);
                CompressedStreamTools.write(data, saveFile);
            }
            return false;
        });
        this.removedSections.forEach(key -> {
            File saveFile = this.getSaveFile(this.getDirectory().getActualDirectory(true), key.x, key.z);
            if (saveFile.exists()) {
                saveFile.delete();
            }
        });
    }

    @Override
    public final void readData() throws IOException {
        File generalSaveFile = new File(this.getDirectory().getActualDirectory(), "general.dat");
        if (generalSaveFile.exists()) {
            CompoundNBT tag = CompressedStreamTools.read(generalSaveFile);
            this.readFromNBT(tag);
        } else {
            this.readFromNBT(new CompoundNBT());
        }

        if (this.loadSectionsInstantly) {
            for (File subFile : this.getDirectory().getActualDirectory().listFiles()) {
                String fileName = subFile.getName();
                if (!fileName.endsWith(".dat")) {
                    continue;
                }
                fileName = fileName.substring(0, fileName.length() - 4);
                String[] ptrn = fileName.split("_");
                if (ptrn.length != 3 || !ptrn[0].equalsIgnoreCase(this.getSaveKey().getIdentifier())) {
                    continue;
                }
                int sX, sZ;
                try {
                    sX = Integer.parseInt(ptrn[1]);
                    sZ = Integer.parseInt(ptrn[2]);
                } catch (NumberFormatException exc) {
                    continue;
                }
                
                WorldCacheIOManager.loadSectionNow(this, sX, sZ,
                        (section) -> this.sections.put(new SectionKey(sX, sZ), section));
            }
        }
    }

    @Nonnull
    public final T loadWorldSection(int sectionX, int sectionZ) {
        File sectionFile = this.getSaveFile(this.getDirectory().getActualDirectory(true), sectionX, sectionZ);
        T newSection = this.createNewSection(sectionX, sectionZ);
        if (!sectionFile.exists()) {
            return newSection;
        }
        try {
            newSection.readFromNBT(CompressedStreamTools.read(sectionFile));
        } catch (IOException e) {
            ObserverLib.log.info("Loading section " + sectionX + ", " + sectionZ + " for " + this.getSaveKey().getIdentifier() + " has failed! Generating empty section!");
            e.printStackTrace();
            newSection = this.createNewSection(sectionX, sectionZ);
        }
        return newSection;
    }

    private static class SectionKey {

        private final int x, z;

        private SectionKey(int x, int z) {
            this.x = x;
            this.z = z;
        }

        private static SectionKey from(WorldSection section) {
            return new SectionKey(section.getSectionX(), section.getSectionZ());
        }

        private static SectionKey resolve(Vec3i absolute, int shift) {
            return new SectionKey(absolute.getX() >> shift, absolute.getZ() >> shift);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SectionKey that = (SectionKey) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

}
