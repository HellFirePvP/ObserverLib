package hellfirepvp.observerlib.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryUtil
 * Created by HellFirePvP
 * Date: 26.08.2020 / 11:36
 */
public class RegistryUtil {

    private final RegistryAccess registries;

    private RegistryUtil(RegistryAccess registries) {
        this.registries = registries;
    }

    public static RegistryUtil side(@Nonnull LogicalSide side) {
        if (side.isServer()) {
            return server();
        } else {
            return client();
        }
    }

    public static RegistryUtil server() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return new RegistryUtil(builtInAccess());
        }
        return new RegistryUtil(server.registryAccess());
    }

    @OnlyIn(Dist.CLIENT)
    public static RegistryUtil client() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return new RegistryUtil(builtInAccess());
        }
        return new RegistryUtil(mc.getConnection().registryAccess());
    }

    private static RegistryAccess builtInAccess() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Nullable
    public <V> ResourceKey<V> getRegistryKey(@Nonnull ResourceKey<Registry<V>> registry, V value) {
        return this.registries.registryOrThrow(registry).getResourceKey(value).orElse(null);
    }

    @Nullable
    public <V> ResourceLocation getKey(@Nonnull ResourceKey<Registry<V>> registry, V value) {
        return this.registries.registryOrThrow(registry).getKey(value);
    }

    public <V> Registry<V> getRegistry(@Nonnull ResourceKey<Registry<V>> registry) {
        return this.registries.registryOrThrow(registry);
    }

    @Nullable
    public <V> V getValue(@Nonnull ResourceKey<Registry<V>> registry, ResourceKey<V> key) {
        return this.registries.registryOrThrow(registry).get(key);
    }

    @Nullable
    public <V> V getValue(@Nonnull ResourceKey<Registry<V>> registry, ResourceLocation key) {
        return this.registries.registryOrThrow(registry).get(key);
    }

    public <V> Collection<Map.Entry<ResourceKey<V>, V>> getEntries(@Nonnull ResourceKey<Registry<V>> registry) {
        return this.registries.registryOrThrow(registry).entrySet();
    }

    public <V> Collection<ResourceLocation> getKeys(@Nonnull ResourceKey<Registry<V>> registry) {
        return this.registries.registryOrThrow(registry).keySet();
    }

    public <V> Collection<V> getValues(@Nonnull ResourceKey<Registry<V>> registry) {
        return this.getEntries(registry).stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public RegistryAccess getRegistryAccess() {
        return registries;
    }
}
