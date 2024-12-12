package hellfirepvp.observerlib.common.data;

import com.google.common.io.Files;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.ObserverLib;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheDomain
 * Created by HellFirePvP
 * Date: 03.07.2019 / 14:57
 */
public class WorldCacheDomain {

    private final ResourceLocation key;
    private final Set<SaveKey<? extends CachedWorldData<?>>> knownSaveKeys = new HashSet<>();
    private final Map<ResourceLocation, Map<SaveKey<?>, CachedWorldData<?>>> domainData = new HashMap<>();

    WorldCacheDomain(ResourceLocation key) {
        this.key = key;
    }

    public <T extends CachedWorldData<T>> SaveKey<T> createSaveKey(String name, Codec<T> dataCodec, Function<SaveKey<T>, T> dataProvider) {
        for (SaveKey<?> key : knownSaveKeys) {
            if (key.identifier.equalsIgnoreCase(name)) {
                return (SaveKey<T>) key;
            }
        }

        SaveKey<T> key = new SaveKey<>(this.getName(), name, dataCodec, dataProvider);
        this.knownSaveKeys.add(key);
        return key;
    }

    @Nullable
    public <T extends CachedWorldData<T>> SaveKey<T> getKey(String identifier) {
        for (SaveKey<?> key : knownSaveKeys) {
            if (key.identifier.equalsIgnoreCase(identifier)) {
                return (SaveKey<T>) key;
            }
        }
        return null;
    }

    @Nonnull
    public Set<SaveKey<? extends CachedWorldData<?>>> getKnownSaveKeys() {
        return Collections.unmodifiableSet(knownSaveKeys);
    }

    public ResourceLocation getName() {
        return key;
    }

    @Nullable
    <T extends CachedWorldData<T>> T getCachedData(ResourceLocation dimTypeName, SaveKey<?> key) {
        return (T) domainData.getOrDefault(dimTypeName, Collections.emptyMap()).get(key);
    }

    @Nullable
    private <T extends CachedWorldData<T>> T getFromCache(Level world, SaveKey<T> key) {
        return getCachedData(world.dimension().location(), key);
    }

    Collection<ResourceLocation> getUsedWorlds() {
        return this.domainData.keySet();
    }

    @Nonnull
    public <T extends CachedWorldData<T>> T getData(Level world, SaveKey<T> key) {
        T data = getFromCache(world, key);
        if (data == null) {
            data = WorldCacheIOThread.loadNow(this, world, key);

            this.domainData.computeIfAbsent(world.dimension().location(), i -> new HashMap<>())
                    .put(key, data);
        }
        return data;
    }

    public File getSaveDirectory() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        File dataDir = server.getWorldPath(new LevelResource(key.getNamespace())).toFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    void clear() {
        this.domainData.clear();
    }

    public static class SaveKey<T extends IWorldRelatedData<T>> {

        public static final Codec<SaveKey<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("domain").forGetter(SaveKey::getDomainName),
                Codec.STRING.fieldOf("identifier").forGetter(SaveKey::getIdentifier)
        ).apply(inst, (domainKey, id) -> WorldCacheManager.findDomain(domainKey)
                .map(domain -> domain.getKey(id))
                .orElseThrow(() -> new IllegalArgumentException("Unknown domain key: " + domainKey + " / " + id))));

        private final ResourceLocation domainName;
        private final String identifier;
        private final Codec<T> instanceCodec;
        private final Function<SaveKey<T>, T> instanceProvider;

        private SaveKey(ResourceLocation domainName, String identifier, Codec<T> instanceCodec, Function<SaveKey<T>, T> provider) {
            this.domainName = domainName;
            this.identifier = identifier;
            this.instanceCodec = instanceCodec;
            this.instanceProvider = provider;
        }

        public ResourceLocation getDomainName() {
            return domainName;
        }

        public String getIdentifier() {
            return identifier;
        }

        public Codec<T> getInstanceCodec() {
            return instanceCodec;
        }

        public T getNewInstance(SaveKey<T> key) {
            return instanceProvider.apply(key);
        }

        public File getSaveFile(File directory) {
            return directory.toPath().resolve(this.getIdentifier() + ".dat").toFile();
        }

        public File createAndBackupSaveFile(File saveDir, File backupDir) throws IOException {
            return this.createAndBackupSaveFile(saveDir, backupDir, this::getSaveFile);
        }

        public File createAndBackupSaveFile(File saveDir, File backupDir, Function<File, File> fileResolver) throws IOException {
            File saveFile = this.getSaveFile(saveDir);
            if (saveFile.exists()) {
                try {
                    Files.copy(saveFile, fileResolver.apply(backupDir));
                } catch (Exception exc) {
                    ObserverLib.log.info("Copying '{}' 's actual file to its backup file failed!", this.getIdentifier());
                    exc.printStackTrace();
                }
            } else {
                saveFile.createNewFile();
            }
            return saveFile;
        }
    }
}
