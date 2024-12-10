package hellfirepvp.observerlib.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class CodecUtil {

    public static <T> Codec<Holder<T>> holderCodec(Registry<T> registry) {
        RegistryHelper<T> regHelper = RegistryHelper.of(registry);
        return ResourceLocation.CODEC.flatXmap(key -> {
            Holder<T> ref = regHelper.getOrNull(key);
            if (ref == null) {
                return DataResult.error(() -> "Unknown registry key: " + key);
            }
            return DataResult.success(ref);
        }, holder -> {
            ResourceKey<T> key = regHelper.unwrapOrNull(holder);
            if (key == null) {
                return DataResult.error(() -> "Unknown registry holder: " + holder);
            }
            return DataResult.success(key.location());
        });
    }

    public static <T> Codec<T> registryNameCodec(Registry<T> registry) {
        RegistryHelper<T> regHelper = RegistryHelper.of(registry);
        return holderCodec(registry).flatXmap(holder -> {
            return DataResult.success(holder.value());
        }, value -> {
            Holder<T> holder = regHelper.getOrNull(value);
            if (holder == null) {
                return DataResult.error(() -> "Unknown registry value: " + value);
            }
            return DataResult.success(holder);
        });
    }

    public static <T> T cast(Object obj) {
        return (T) obj;
    }
}
