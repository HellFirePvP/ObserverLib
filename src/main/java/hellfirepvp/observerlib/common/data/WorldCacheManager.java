package hellfirepvp.observerlib.common.data;

import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheManager
 * Created by HellFirePvP
 * Date: 02.08.2016 / 23:15
 */
public class WorldCacheManager implements ITickHandler {

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
                    CachedWorldData data = domain.getCachedData(dimTypeName, key);
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
        ResourceLocation domainKey = new ResourceLocation(modid, DEFAULT_DOMAIN_NAME);
        WorldCacheDomain domain = new WorldCacheDomain(domainKey);
        domains.put(domainKey, domain);
        return domain;
    }

    @Nullable
    public static WorldCacheDomain findDomain(String modid) {
        ResourceLocation domainKey = new ResourceLocation(modid, DEFAULT_DOMAIN_NAME);
        for (ResourceLocation key : domains.keySet()) {
            if (key.equals(domainKey)) {
                return domains.get(key);
            }
        }
        return null;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Level world = (Level) context[0];
        if (world.isClientSide) return;
        for (WorldCacheDomain domain : domains.values()) {
            domain.tick(world);
        }
    }

    public void doSave(Level world) {
        ResourceLocation worldName = world.dimension().location();
        for (WorldCacheDomain domain : domains.values()) {
            for (WorldCacheDomain.SaveKey<?> key : domain.getKnownSaveKeys()) {
                CachedWorldData data = domain.getCachedData(worldName, key);
                if (data != null && data.needsSaving()) {
                    WorldCacheIOThread.scheduleSave(domain, worldName, data);
                }
            }
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.WORLD);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "WorldCacheManager";
    }

}
