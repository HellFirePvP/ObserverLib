package hellfirepvp.observerlib.common.data;

import hellfirepvp.observerlib.common.data.io.DirectorySet;
import hellfirepvp.observerlib.common.data.io.WorldCacheIOManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheDomain
 * Created by HellFirePvP
 * Date: 03.07.2019 / 14:57
 */
public class WorldCacheDomain {

    private final ResourceLocation key;
    private Set<SaveKey<? extends CachedWorldData>> knownSaveKeys = new HashSet<>();

    private final Object worldDataLck = new Object();
    private Map<ResourceLocation, Map<SaveKey<?>, CachedWorldData>> loadedWorldData = new HashMap<>();

    WorldCacheDomain(ResourceLocation key) {
        this.key = key;
    }

    public <T extends CachedWorldData> SaveKey<T> createSaveKey(String name, BiFunction<SaveKey<T>, DirectorySet, T> dataProvider) {
        for (SaveKey<?> key : knownSaveKeys) {
            if (key.identifier.equalsIgnoreCase(name)) {
                return (SaveKey<T>) key;
            }
        }

        SaveKey<T> key = new SaveKey<>(name, dataProvider);
        this.knownSaveKeys.add(key);
        return key;
    }

    @Nullable
    public <T extends CachedWorldData> SaveKey<T> getKey(String identifier) {
        for (SaveKey<?> key : knownSaveKeys) {
            if (key.identifier.equalsIgnoreCase(identifier)) {
                return (SaveKey<T>) key;
            }
        }
        return null;
    }

    @Nonnull
    public Set<SaveKey<? extends CachedWorldData>> getKnownSaveKeys() {
        return Collections.unmodifiableSet(knownSaveKeys);
    }

    Collection<ResourceLocation> getUsedWorlds() {
        synchronized (this.worldDataLck) {
            return this.loadedWorldData.keySet();
        }
    }

    public ResourceLocation getName() {
        return key;
    }

    void tick(World world) {
        synchronized (this.worldDataLck) {
            ResourceLocation dimKey = world.getDimension().getType().getRegistryName();
            if (!this.loadedWorldData.containsKey(dimKey)) {
                return;
            }

            Map<SaveKey<?>, ? extends CachedWorldData> dataMap = this.loadedWorldData.get(dimKey);
            for (WorldCacheDomain.SaveKey<?> key : this.getKnownSaveKeys()) {
                if (dataMap.containsKey(key)) {
                    dataMap.get(key).updateTick(world);
                }
            }
        }
    }

    @Nullable
    public <T extends CachedWorldData> T getCachedData(IWorld world, SaveKey<T> key) {
        return this.getCachedData(world.getDimension().getType().getRegistryName(), key);
    }

    @Nullable
    public <T extends CachedWorldData> T getCachedData(ResourceLocation dimKey, SaveKey<T> key) {
        synchronized (this.worldDataLck) {
            if (!this.loadedWorldData.containsKey(dimKey)) {
                return null;
            }
            return (T) this.loadedWorldData.get(dimKey).get(key);
        }
    }

    public <T extends CachedWorldData> void getData(IWorld world, SaveKey<T> key, Consumer<T> onLoad) {
        T data = getCachedData(world, key);
        if (data != null) {
            onLoad.accept(data);
            return;
        }

        ResourceLocation dimKey = world.getDimension().getType().getRegistryName();
        Consumer<T> cacheFn = (loaded) -> {
            synchronized (this.worldDataLck) {
                this.loadedWorldData.computeIfAbsent(dimKey, i -> new HashMap<>()).putIfAbsent(key, loaded);
            }
        };
        WorldCacheIOManager.scheduleCacheLoad(this, dimKey, key, cacheFn.andThen(loaded -> loaded.onLoad(world)).andThen(onLoad));
    }

    public File getSaveDirectory() {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        if (server == null) {
            return null;
        }
        File dataDir = server.getActiveAnvilConverter().getFile(server.getFolderName(), key.getNamespace());
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    void clear() {
        synchronized (this.worldDataLck) {
            this.loadedWorldData.clear();
        }
    }

    public static class SaveKey<T extends CachedWorldData> {

        private final String identifier;
        private final BiFunction<SaveKey<T>, DirectorySet, T> instanceProvider;

        private SaveKey(String identifier, BiFunction<SaveKey<T>, DirectorySet, T> provider) {
            this.identifier = identifier;
            this.instanceProvider = provider;
        }

        public T getNewInstance(SaveKey<T> key, DirectorySet directory) {
            return instanceProvider.apply(key, directory);
        }

        public String getIdentifier() {
            return identifier;
        }

    }

}
