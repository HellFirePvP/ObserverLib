package hellfirepvp.observerlib.common.registry;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryStructures
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:43
 */
public class RegistryStructures {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ObserverLib.MODID, "matchable_structures");
    private static Supplier<IForgeRegistry<MatchableStructure>> REGISTRY;

    public static void initialize(NewRegistryEvent event) {
        REGISTRY = event.create(new RegistryBuilder<MatchableStructure>()
                .setName(REGISTRY_NAME)
                .setType(MatchableStructure.class));
    }

    @Nullable
    public static MatchableStructure getStructure(ResourceLocation key) {
        return REGISTRY.get().getValue(key);
    }

    @Nonnull
    public static Collection<MatchableStructure> getAll() {
        return REGISTRY.get().getValues();
    }

}
