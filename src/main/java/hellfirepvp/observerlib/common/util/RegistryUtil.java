package hellfirepvp.observerlib.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

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

    private final DynamicRegistries registries;

    private RegistryUtil(DynamicRegistries registries) {
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
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        if (server == null) {
            return new RegistryUtil(DynamicRegistries.func_239770_b_());
        }
        return new RegistryUtil(server.func_244267_aX());
    }

    @OnlyIn(Dist.CLIENT)
    public static RegistryUtil client() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return new RegistryUtil(DynamicRegistries.func_239770_b_());
        }
        return new RegistryUtil(mc.getConnection().func_239165_n_());
    }

    @Nullable
    public <V> RegistryKey<V> getRegistryKey(@Nonnull RegistryKey<Registry<V>> registry, V value) {
        return this.registries.getRegistry(registry).getOptionalKey(value).orElse(null);
    }

    @Nullable
    public <V> ResourceLocation getKey(@Nonnull RegistryKey<Registry<V>> registry, V value) {
        return this.registries.getRegistry(registry).getKey(value);
    }

    @Nullable
    public <V> V getValue(@Nonnull RegistryKey<Registry<V>> registry, RegistryKey<V> key) {
        return this.registries.getRegistry(registry).getValueForKey(key);
    }

    @Nullable
    public <V> V getValue(@Nonnull RegistryKey<Registry<V>> registry, ResourceLocation key) {
        return this.registries.getRegistry(registry).getOrDefault(key);
    }

    public <V> Collection<Map.Entry<RegistryKey<V>, V>> getEntries(@Nonnull RegistryKey<Registry<V>> registry) {
        return this.registries.getRegistry(registry).getEntries();
    }

    public <V> Collection<ResourceLocation> getKeys(@Nonnull RegistryKey<Registry<V>> registry) {
        return this.registries.getRegistry(registry).keySet();
    }

    public <V> Collection<V> getValues(@Nonnull RegistryKey<Registry<V>> registry) {
        return this.getEntries(registry).stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
