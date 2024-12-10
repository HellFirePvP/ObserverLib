package hellfirepvp.observerlib.common.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryLookup
 * Created by HellFirePvP
 * Date: 09.12.2024 / 23:44
 */
public class RegistryHelper<T> {

    private final Registry<T> registry;

    private RegistryHelper(Registry<T> registry) {
        this.registry = registry;
    }

    public static <T> RegistryHelper<T> of(Registry<T> registry) {
        return new RegistryHelper<>(registry);
    }

    @Nonnull
    public ResourceKey<T> unwrapOrThrow(Holder<T> holder) {
        return holder.unwrap()
                .mapRight(pValue -> registry.getResourceKey(pValue).orElseThrow())
                .map(Function.identity(), Function.identity());
    }

    @Nullable
    public ResourceKey<T> unwrapOrNull(Holder<T> holder) {
        return holder.unwrap()
                .mapRight(pValue -> registry.getResourceKey(pValue).orElse(null))
                .map(Function.identity(), Function.identity());
    }

    @Nonnull
    public Holder<T> getOrThrow(T value) {
        ResourceKey<T> typeKey = this.registry.getResourceKey(value).orElse(null);
        if (typeKey == null) {
            throw new IllegalArgumentException("Value not registered! " + this.getClass().getName());
        }
        return this.getOrThrow(typeKey);
    }

    @Nonnull
    public Holder<T> getOrThrow(String key) {
        return this.getOrThrow(ResourceLocation.parse(key));
    }

    @Nonnull
    public Holder<T> getOrThrow(ResourceLocation key) {
        return this.getOrThrow(ResourceKey.create(this.registry.key(), key));
    }
    @Nonnull
    public Holder<T> getOrThrow(ResourceKey<T> key) {
        return this.registry.getHolderOrThrow(key);
    }

    @Nullable
    public Holder<T> getOrNull(T value) {
        ResourceKey<T> typeKey = this.registry.getResourceKey(value).orElse(null);
        if (typeKey == null) {
            return null;
        }
        return this.getOrNull(typeKey);
    }

    @Nullable
    public Holder<T> getOrNull(String key) {
        return this.getOrNull(ResourceLocation.parse(key));
    }

    @Nullable
    public Holder<T> getOrNull(ResourceLocation key) {
        return this.getOrNull(ResourceKey.create(this.registry.key(), key));
    }

    @Nullable
    public Holder<T> getOrNull(ResourceKey<T> key) {
        return this.registry.getHolder(key).orElse(null);
    }
}
