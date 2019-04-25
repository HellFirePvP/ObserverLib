package hellfirepvp.observerlib.common.registry;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryStructures
 * Created by HellFirePvP
 * Date: 25.04.2019 / 20:43
 */
public class RegistryStructures {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ObserverLib.MODID, "matchable_structures");
    private static IForgeRegistryModifiable<MatchableStructure> REGISTRY;

    public static void initialize() {
        REGISTRY = (IForgeRegistryModifiable<MatchableStructure>) new RegistryBuilder<MatchableStructure>()
                .setName(REGISTRY_NAME)
                .setType(MatchableStructure.class)
                .create();
    }

    @Nullable
    public static MatchableStructure getStructure(ResourceLocation key) {
        return REGISTRY.getValue(key);
    }

    @Nonnull
    public static Collection<MatchableStructure> getAll() {
        return REGISTRY.getValues();
    }

}
