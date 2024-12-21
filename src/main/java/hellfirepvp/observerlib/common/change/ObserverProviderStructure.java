package hellfirepvp.observerlib.common.change;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.util.CodecUtil;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverProviderStructure
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:16
 */
public class ObserverProviderStructure extends ObserverProvider<ChangeObserverStructure> {

    public static final Codec<ObserverProviderStructure> PROVIDER_CODEC = RegistryProviders.getRegistry().byNameCodec()
            .xmap(provider -> CodecUtil.informedCast(provider, ObserverProviderStructure.class), Function.identity());
    public static final MapCodec<ChangeObserverStructure> OBSERVER_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PROVIDER_CODEC.fieldOf("provider").forGetter(ChangeObserverStructure::getProvider),
            BlockPos.CODEC.listOf().fieldOf("mismatches").forGetter(ChangeObserverStructure::getMismatches)
    ).apply(builder, (provider, mismatches) -> provider.newObserver().addMismatches(mismatches)));

    private final MatchableStructure structure;

    public ObserverProviderStructure(MatchableStructure structure) {
        this.structure = structure;
    }

    public MatchableStructure getStructure() {
        return structure;
    }

    @Override
    public MapCodec<ChangeObserverStructure> codec() {
        return OBSERVER_CODEC;
    }

    @Override
    @Nonnull
    public ChangeObserverStructure newObserver() {
        return new ChangeObserverStructure(this, this.getStructure());
    }
}
