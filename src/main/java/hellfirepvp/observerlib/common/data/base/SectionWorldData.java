package hellfirepvp.observerlib.common.data.base;

import com.google.common.io.Files;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.util.AlternatingSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

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

    private final Map<SectionKey, T> sections = new HashMap<>();
    private final int precision;

    private final AlternatingSet<SectionKey> dirtySections = new AlternatingSet<>();
    private final Set<SectionKey> removedSections = new HashSet<>();

    protected SectionWorldData(WorldCacheDomain.SaveKey<?> key, int sectionPrecision) {
        super(key);
        this.precision = sectionPrecision;
    }

    public void markDirty(Vector3i absolute) {
        SectionKey key = SectionKey.resolve(absolute, this.precision);
        T section = getSection(key);
        if (section != null) {
            this.write(() -> this.dirtySections.add(key));
        }
    }

    public void markDirty(T section) {
        this.write(() -> this.dirtySections.add(SectionKey.from(section)));
    }

    protected abstract T createNewSection(int sectionX, int sectionZ);

    @Nonnull
    public Collection<T> getSections(Vector3i absoluteMin, Vector3i absoluteMax) {
        return resolveSections(absoluteMin, absoluteMax, this::getSection);
    }

    @Nonnull
    public Collection<T> getOrCreateSections(Vector3i absoluteMin, Vector3i absoluteMax) {
        return resolveSections(absoluteMin, absoluteMax, this::getOrCreateSection);
    }

    @Nonnull
    private Collection<T> resolveSections(Vector3i absoluteMin, Vector3i absoluteMax, Function<SectionKey, T> sectionFct) {
        SectionKey lower = SectionKey.resolve(absoluteMin, this.precision);
        SectionKey higher = SectionKey.resolve(absoluteMax, this.precision);
        Collection<T> out = new HashSet<>();
        for (int xx = lower.x; xx <= higher.x; xx++) {
            for (int zz = lower.z; zz <= higher.z; zz++) {
                T section = sectionFct.apply(new SectionKey(xx, zz));
                if (section != null) {
                    out.add(section);
                }
            }
        }
        return out;
    }

    @Nonnull
    public T getOrCreateSection(Vector3i absolute) {
        return getOrCreateSection(SectionKey.resolve(absolute, this.precision));
    }

    @Nonnull
    private T getOrCreateSection(SectionKey key) {
        return this.write(() -> this.sections.computeIfAbsent(key, sectionKey -> createNewSection(sectionKey.x, sectionKey.z)));
    }

    @Nullable
    public T getSection(Vector3i absolute) {
        return this.getSection(SectionKey.resolve(absolute, this.precision));
    }

    @Nullable
    private T getSection(SectionKey key) {
        return this.read(() -> this.sections.get(key));
    }

    public boolean removeSection(T section) {
        SectionKey key = SectionKey.from(section);
        return this.sections.remove(key) == section && this.removedSections.add(key);
    }

    public boolean removeSection(Vector3i absolute) {
        SectionKey key = SectionKey.resolve(absolute, this.precision);
        return this.sections.remove(key) != null && this.removedSections.add(key);
    }

    @Nonnull
    public Collection<T> getSections() {
        return this.sections.values();
    }

    @Override
    public boolean needsSaving() {
        return !this.dirtySections.isEmpty();
    }

    @Override
    public void markSaved() {
        this.write(() -> this.dirtySections.clear());
    }

    public abstract void writeToNBT(CompoundNBT nbt);

    public abstract void readFromNBT(CompoundNBT nbt);

    private File getSaveFile(File directory, T section) {
        String name = String.format("%s_%s_%s.dat",
                this.getSaveKey().getIdentifier(),
                section.getSectionX(),
                section.getSectionZ());
        return directory.toPath().resolve(name).toFile();
    }

    @Override
    public final void writeData(File baseDirectory, File backupDirectory) throws IOException {
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }
        File generalSaveFile = new File(baseDirectory, "general.dat");
        if (generalSaveFile.exists()) {
            try {
                Files.copy(generalSaveFile, new File(backupDirectory, "general.dat"));
            } catch (Exception exc) {
                ObserverLib.log.info("Copying '" + getSaveKey().getIdentifier() + "' general actual file to its backup file failed!");
                exc.printStackTrace();
            }
        } else {
            generalSaveFile.createNewFile();
        }

        CompoundNBT generalData = new CompoundNBT();
        this.readIO(() -> this.writeToNBT(generalData));
        CompressedStreamTools.write(generalData, generalSaveFile);

        Set<SectionKey> sections = new HashSet<>();
        this.dirtySections.forEach(key -> {
            sections.add(key);
            return false;
        });

        for (SectionKey sectionKey : sections) {
            T section = getSection(sectionKey);
            if (section != null) {
                File saveFile = this.getSaveFile(baseDirectory, section);
                if (saveFile.exists()) {
                    try {
                        Files.copy(saveFile, this.getSaveFile(backupDirectory, section));
                    } catch (Exception exc) {
                        ObserverLib.log.info("Copying '" + getSaveKey().getIdentifier() + "' actual file to its backup file failed!");
                        exc.printStackTrace();
                    }
                } else {
                    saveFile.createNewFile();
                }

                CompoundNBT data = new CompoundNBT();
                this.readIO(() -> section.writeToNBT(data));
                CompressedStreamTools.write(data, saveFile);
            }
        }
    }

    @Override
    public final void readData(File baseDirectory) throws IOException {
        String identifier = getSaveKey().getIdentifier();

        File generalSaveFile = new File(baseDirectory, "general.dat");
        if (generalSaveFile.exists()) {
            CompoundNBT tag = CompressedStreamTools.read(generalSaveFile);
            this.writeIO(() -> this.readFromNBT(tag));
        } else {
            this.writeIO(() -> this.readFromNBT(new CompoundNBT()));
        }

        for (File subFile : baseDirectory.listFiles()) {
            String fileName = subFile.getName();
            if (!fileName.endsWith(".dat")) {
                continue;
            }
            fileName = fileName.substring(0, fileName.length() - 4);
            String[] ptrn = fileName.split("_");
            if (ptrn.length != 3 || !ptrn[0].equalsIgnoreCase(identifier)) {
                continue;
            }
            int sX, sZ;
            try {
                sX = Integer.parseInt(ptrn[1]);
                sZ = Integer.parseInt(ptrn[2]);
            } catch (NumberFormatException exc) {
                continue;
            }

            this.writeIO(() -> {
                T section = createNewSection(sX, sZ);
                section.readFromNBT(CompressedStreamTools.read(subFile));
                this.sections.put(new SectionKey(sX, sZ), section);
            });
        }
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

        private static SectionKey resolve(Vector3i absolute, int shift) {
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
