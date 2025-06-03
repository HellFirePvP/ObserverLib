package hellfirepvp.observerlib.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;

import java.util.function.Function;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on GitHub.
 * Class: StringCodecs
 * Created by HellFirePvP
 * Date: 03.06.2025 / 14:24
 */
public class StringCodecs {

    public static Codec<String> sized(int maxLength) {
        return sized(0, maxLength);
    }

    public static Codec<String> sized(int minLength, int maxLength) {
        return Codec.STRING.comapFlatMap(str -> {
            if (str.length() < minLength || str.length() > maxLength) {
                return DataResult.error(() -> "String length out of bounds: " + str.length() + ", expected range [" + minLength + "-" + maxLength + "]");
            }
            return DataResult.success(str);
        }, Function.identity());
    }

    public static Codec<BlockPos> blockPos() {
        return stringifyCodec(str -> {
            String[] parts = str.split(";");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid BlockPos string format: " + str);
            }
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }, pos -> String.format("%s;%s;%s", pos.getX(), pos.getY(), pos.getZ()));
    }

    public static <T> Codec<T> stringifyCodec(Function<String, T> fromString, Function<T, String> toString) {
        return Codec.STRING.comapFlatMap(str -> {
            try {
                return DataResult.success(fromString.apply(str));
            } catch (Exception e) {
                return DataResult.error(() -> "Failed to parse string: " + str + " - " + e.getMessage());
            }
        }, toString);
    }
}
