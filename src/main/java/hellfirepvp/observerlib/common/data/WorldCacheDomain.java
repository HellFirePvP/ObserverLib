package hellfirepvp.observerlib.common.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheDomain
 * Created by HellFirePvP
 * Date: 03.07.2019 / 14:57
 */
public class WorldCacheDomain {

    private final ResourceLocation key;
    private Set<SaveKey> knownSaveKeys = new HashSet<>();
    private Map<Integer, Map<SaveKey, CachedWorldData>> domainData = new HashMap<>();

    WorldCacheDomain(ResourceLocation key) {
        this.key = key;
    }

    public SaveKey createSaveKey(String name, Function<SaveKey, CachedWorldData> dataProvider) {
        for (SaveKey key : knownSaveKeys) {
            if (key.identifier.equalsIgnoreCase(name)) {
                return key;
            }
        }

        SaveKey key = new SaveKey(name, dataProvider);
        this.knownSaveKeys.add(key);
        return key;
    }

    @Nullable
    public SaveKey getKey(String identifier) {
        for (SaveKey key : knownSaveKeys) {
            if (key.identifier.equalsIgnoreCase(identifier)) {
                return key;
            }
        }
        return null;
    }

    @Nonnull
    public Set<SaveKey> getKnownSaveKeys() {
        return Collections.unmodifiableSet(knownSaveKeys);
    }

    public ResourceLocation getName() {
        return key;
    }

    void tick(World world) {
        int dimId = world.getDimension().getType().getId();
        if (!this.domainData.containsKey(dimId)) {
            return;
        }

        Map<SaveKey, CachedWorldData> dataMap = this.domainData.get(dimId);
        for (WorldCacheDomain.SaveKey key : this.getKnownSaveKeys()) {
            if(dataMap.containsKey(key)) {
                dataMap.get(key).updateTick(world);
            }
        }
    }

    @Nullable
    <T extends CachedWorldData> T getCachedData(int dimId, SaveKey key) {
        if (!domainData.containsKey(dimId)) {
            return null;
        }
        return (T) domainData.get(dimId).get(key);
    }

    Collection<Integer> getUsedWorlds() {
        return this.domainData.keySet();
    }

    @Nonnull
    public <T extends CachedWorldData> T getData(World world, SaveKey key) {
        CachedWorldData data = getFromCache(world, key);
        if(data == null) {
            data = WorldCacheIOThread.loadNow(this, world, key);

            int dimId = world.getDimension().getType().getId();
            this.domainData.computeIfAbsent(dimId, i -> new HashMap<>())
                    .put(key, data);
        }
        return (T) data;
    }

    @Nullable
    private CachedWorldData getFromCache(World world, SaveKey key) {
        int dimId = world.getDimension().getType().getId();
        if (!domainData.containsKey(dimId)) {
            return null;
        }
        Map<SaveKey, CachedWorldData> dataMap = domainData.get(dimId);
        return dataMap.get(key);
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

    public static class SaveKey {

        private final String identifier;
        private final Function<SaveKey, CachedWorldData> instanceProvider;

        private SaveKey(String identifier, Function<SaveKey, CachedWorldData> provider) {
            this.identifier = identifier;
            this.instanceProvider = provider;
        }

        public CachedWorldData getNewInstance(SaveKey key) {
            return instanceProvider.apply(key);
        }

        public String getIdentifier() {
            return identifier;
        }

    }

}
