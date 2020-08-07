package hellfirepvp.observerlib.common.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
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
    private Set<SaveKey<? extends CachedWorldData>> knownSaveKeys = new HashSet<>();
    private Map<ResourceLocation, Map<SaveKey<?>, CachedWorldData>> domainData = new HashMap<>();

    WorldCacheDomain(ResourceLocation key) {
        this.key = key;
    }

    public <T extends CachedWorldData> SaveKey<T> createSaveKey(String name, Function<SaveKey<T>, T> dataProvider) {
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

    public ResourceLocation getName() {
        return key;
    }

    void tick(World world) {
        ResourceLocation dimTypeName = world.getWorld().func_234922_V_().func_240901_a_();
        if (!this.domainData.containsKey(dimTypeName)) {
            return;
        }

        Map<SaveKey<?>, ? extends CachedWorldData> dataMap = this.domainData.get(dimTypeName);
        for (WorldCacheDomain.SaveKey<?> key : this.getKnownSaveKeys()) {
            if (dataMap.containsKey(key)) {
                dataMap.get(key).updateTick(world);
            }
        }
    }

    @Nullable
    <T extends CachedWorldData> T getCachedData(ResourceLocation dimTypeName, SaveKey<T> key) {
        return (T) domainData.getOrDefault(dimTypeName, Collections.emptyMap()).get(key);
    }

    @Nullable
    private <T extends CachedWorldData> T getFromCache(IWorld world, SaveKey<T> key) {
        ResourceLocation dimTypeName = world.getWorld().func_234922_V_().func_240901_a_();
        return getCachedData(dimTypeName, key);
    }

    Collection<ResourceLocation> getUsedWorlds() {
        return this.domainData.keySet();
    }

    @Nonnull
    public <T extends CachedWorldData> T getData(IWorld world, SaveKey<T> key) {
        T data = getFromCache(world, key);
        if (data == null) {
            data = WorldCacheIOThread.loadNow(this, world, key);

            ResourceLocation dimTypeName = world.getWorld().func_234922_V_().func_240901_a_();
            this.domainData.computeIfAbsent(dimTypeName, i -> new HashMap<>())
                    .put(key, data);
        }
        return (T) data;
    }

    public File getSaveDirectory() {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        if (server == null) {
            return null;
        }
        File dataDir = server.func_240776_a_(new FolderName(key.getNamespace())).toFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    void clear() {
        this.domainData.clear();
    }

    public static class SaveKey<T extends CachedWorldData> {

        private final String identifier;
        private final Function<SaveKey<T>, T> instanceProvider;

        private SaveKey(String identifier, Function<SaveKey<T>, T> provider) {
            this.identifier = identifier;
            this.instanceProvider = provider;
        }

        public T getNewInstance(SaveKey<T> key) {
            return instanceProvider.apply(key);
        }

        public String getIdentifier() {
            return identifier;
        }

    }

}
