package hellfirepvp.observerlib.common.data.base;

import com.google.common.io.Files;
import com.mojang.serialization.Codec;
import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.util.AlternatingSet;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

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
public abstract class SectionWorldData<T extends SectionWorldData<T, S>, S extends WorldSection> extends CachedWorldData<T> {

    public static final int PRECISION_REGION = 9;
    public static final int PRECISION_AREA = 6;
    public static final int PRECISION_SECTION = 5;
    public static final int PRECISION_CHUNK = 4;

    private final Map<SectionKey, S> sections = new HashMap<>();
    private final Codec<S> sectionCodec;
    private final int precision;

    private final AlternatingSet<SectionKey> dirtySections = new AlternatingSet<>();

    protected SectionWorldData(WorldCacheDomain.SaveKey<T> key, Codec<S> sectionCodec, int sectionPrecision) {
        super(key);
        this.sectionCodec = sectionCodec;
        this.precision = sectionPrecision;
    }

    public void markDirty(Vec3i absolute) {
        SectionKey key = SectionKey.resolve(absolute, this.precision);
        S section = getSection(key);
        if (section != null) {
            this.write(() -> this.dirtySections.add(key));
        }
    }

    public void markDirty(S section) {
        this.write(() -> this.dirtySections.add(SectionKey.from(section)));
    }

    protected abstract S createNewSection(int sectionX, int sectionZ);

    @Nonnull
    public Collection<S> getSections(Vec3i absoluteMin, Vec3i absoluteMax) {
        return resolveSections(absoluteMin, absoluteMax, this::getSection);
    }

    @Nonnull
    public Collection<S> getOrCreateSections(Vec3i absoluteMin, Vec3i absoluteMax) {
        return resolveSections(absoluteMin, absoluteMax, this::getOrCreateSection);
    }

    @Nonnull
    private Collection<S> resolveSections(Vec3i absoluteMin, Vec3i absoluteMax, Function<SectionKey, S> sectionFct) {
        SectionKey lower = SectionKey.resolve(absoluteMin, this.precision);
        SectionKey higher = SectionKey.resolve(absoluteMax, this.precision);
        Collection<S> out = new HashSet<>();
        for (int xx = lower.x; xx <= higher.x; xx++) {
            for (int zz = lower.z; zz <= higher.z; zz++) {
                S section = sectionFct.apply(new SectionKey(xx, zz));
                if (section != null) {
                    out.add(section);
                }
            }
        }
        return out;
    }

    @Nonnull
    public S getOrCreateSection(Vec3i absolute) {
        return getOrCreateSection(SectionKey.resolve(absolute, this.precision));
    }

    @Nonnull
    private S getOrCreateSection(SectionKey key) {
        return this.write(() -> this.sections.computeIfAbsent(key, sectionKey -> createNewSection(sectionKey.x, sectionKey.z)));
    }

    @Nullable
    public S getSection(Vec3i absolute) {
        return this.getSection(SectionKey.resolve(absolute, this.precision));
    }

    @Nullable
    private S getSection(SectionKey key) {
        return this.read(() -> this.sections.get(key));
    }

    public boolean removeSection(S section) {
        SectionKey key = SectionKey.from(section);
        return this.sections.remove(key) != null;
    }

    public boolean removeSection(Vec3i absolute) {
        SectionKey key = SectionKey.resolve(absolute, this.precision);
        return this.sections.remove(key) != null;
    }

    @Nonnull
    public Collection<S> getSections() {
        return this.sections.values();
    }

    @Override
    public boolean needsSaving() {
        return !this.dirtySections.isEmpty();
    }

    @Override
    public void markSaved() {
        this.write(this.dirtySections::clear);
    }

    private File getSectionSaveFile(File directory, S section) {
        String name = String.format("%s_%s_%s.dat",
                this.getSaveKey().getIdentifier(),
                section.getSectionX(),
                section.getSectionZ());
        return directory.toPath().resolve(name).toFile();
    }

    @Override
    public void writeAdditionalData(File saveDir, File backupDir) throws IOException {
        Set<SectionKey> sections = new HashSet<>();
        this.dirtySections.forEach(key -> {
            sections.add(key);
            return false;
        });

        for (SectionKey sectionKey : sections) {
            S section = getSection(sectionKey);
            if (section != null) {
                File saveFile = this.getSaveKey().createAndBackupSaveFile(saveDir, backupDir,
                        dir -> this.getSectionSaveFile(dir, section));

                this.writeIO(() -> {
                    CompoundTag data = new CompoundTag();
                    data.put("data", this.sectionCodec.encodeStart(NbtOps.INSTANCE, section).getOrThrow());
                    NbtIo.write(data, saveFile.toPath());
                });
            }
        }
    }

    @Override
    public void readAdditionalData(File dir) throws IOException {
        String identifier = getSaveKey().getIdentifier();
        for (File subFile : dir.listFiles()) {
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

            this.readIO(() -> {
                CompoundTag data = NbtIo.read(subFile.toPath());
                this.sectionCodec.parse(NbtOps.INSTANCE, data.get("data")).ifSuccess(section -> {
                    this.sections.put(new SectionKey(sX, sZ), section);
                });
            });
        }
    }

    private record SectionKey(int x, int z) {

        private static SectionKey from(WorldSection section) {
            return new SectionKey(section.getSectionX(), section.getSectionZ());
        }

        private static SectionKey resolve(Vec3i absolute, int shift) {
            return new SectionKey(absolute.getX() >> shift, absolute.getZ() >> shift);
        }
    }
}
