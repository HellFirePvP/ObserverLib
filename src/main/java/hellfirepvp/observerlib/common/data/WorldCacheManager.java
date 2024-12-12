package hellfirepvp.observerlib.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheManager
 * Created by HellFirePvP
 * Date: 02.08.2016 / 23:15
 */
public class WorldCacheManager {

    private static final String DEFAULT_DOMAIN_NAME = "worlddata";

    private static final WorldCacheManager instance = new WorldCacheManager();
    private static final Map<ResourceLocation, WorldCacheDomain> domains = new HashMap<>();

    private WorldCacheManager() {}

    public static WorldCacheManager getInstance() {
        return instance;
    }

    public static void scheduleSaveAll() {
        for (WorldCacheDomain domain : domains.values()) {
            for (ResourceLocation dimTypeName : domain.getUsedWorlds()) {
                for (WorldCacheDomain.SaveKey<?> key : domain.getKnownSaveKeys()) {
                    CachedWorldData<?> data = domain.getCachedData(dimTypeName, key);
                    if (data != null && data.needsSaving()) {
                        WorldCacheIOThread.scheduleSave(domain, dimTypeName, data);
                    }
                }
            }
        }
    }

    public static void cleanUp() {
        for (WorldCacheDomain domain : domains.values()) {
            domain.clear();
        }
    }

    @Nonnull
    public static WorldCacheDomain createDomain(String modid) {
        ResourceLocation domainKey = ResourceLocation.fromNamespaceAndPath(modid, DEFAULT_DOMAIN_NAME);
        WorldCacheDomain domain = new WorldCacheDomain(domainKey);
        domains.put(domainKey, domain);
        return domain;
    }

    public static Optional<WorldCacheDomain> findDomain(String modid) {
        return findDomain(ResourceLocation.fromNamespaceAndPath(modid, DEFAULT_DOMAIN_NAME));
    }

    public static Optional<WorldCacheDomain> findDomain(ResourceLocation domainKey) {
        for (ResourceLocation key : domains.keySet()) {
            if (key.equals(domainKey)) {
                return Optional.of(domains.get(key));
            }
        }
        return Optional.empty();
    }

    public void doSave(Level world) {
        ResourceLocation worldName = world.dimension().location();
        for (WorldCacheDomain domain : domains.values()) {
            for (WorldCacheDomain.SaveKey<?> key : domain.getKnownSaveKeys()) {
                CachedWorldData<?> data = domain.getCachedData(worldName, key);
                if (data != null && data.needsSaving()) {
                    WorldCacheIOThread.scheduleSave(domain, worldName, data);
                }
            }
        }
    }
}
