package hellfirepvp.observerlib.common.registry;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryStructures
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:43
 */
public class RegistryStructures {

    public static final ResourceKey<Registry<MatchableStructure>> REGISTRY_KEY = ResourceKey.createRegistryKey(ObserverLib.key("matchable_structures"));
    private static Registry<MatchableStructure> REGISTRY;

    public static void initialize(NewRegistryEvent event) {
        REGISTRY = event.create(new RegistryBuilder<>(REGISTRY_KEY));
    }

    @Nullable
    public static MatchableStructure getStructure(ResourceLocation key) {
        return REGISTRY.get(key);
    }

    @Nonnull
    public static Registry<MatchableStructure> getAll() {
        return REGISTRY;
    }

}
