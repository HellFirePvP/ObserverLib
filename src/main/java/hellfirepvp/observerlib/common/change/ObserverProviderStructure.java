package hellfirepvp.observerlib.common.change;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import hellfirepvp.observerlib.common.registry.RegistryStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ObserverProviderStructure
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:16
 */
public class ObserverProviderStructure extends ObserverProvider<ChangeObserverStructure> {

    public static final MapCodec<ChangeObserverStructure> OBSERVER_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(observer -> observer.getProvider().getStructureName()),
            BlockPos.CODEC.listOf().fieldOf("mismatches").forGetter(ChangeObserverStructure::getMismatches)
    ).apply(builder, (structure, mismatches) -> new ObserverProviderStructure(structure).newObserver().addMismatches(mismatches)));

    private final ResourceLocation structureName;

    public ObserverProviderStructure(ResourceLocation structureName) {
        this.structureName = structureName;
    }

    public ResourceLocation getStructureName() {
        return this.structureName;
    }

    @Override
    public MapCodec<ChangeObserverStructure> codec() {
        return OBSERVER_CODEC;
    }

    @Override
    @Nonnull
    public ChangeObserverStructure newObserver() {
        MatchableStructure structure = RegistryStructures.getStructure(this.getStructureName());
        if (structure == null) {
            throw new IllegalStateException("Tried creating structure observer for unknown structure: " + this.getStructureName());
        }
        return new ChangeObserverStructure(this, structure);
    }
}
