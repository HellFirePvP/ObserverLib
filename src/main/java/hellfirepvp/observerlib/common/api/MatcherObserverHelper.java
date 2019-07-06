package hellfirepvp.observerlib.common.api;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.api.structure.ObserverProvider;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.WorldCacheManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: MatcherObserverHelper
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:24
 */
public class MatcherObserverHelper extends ObserverHelper {

    private static WorldCacheDomain WORLD_DOMAIN = WorldCacheManager.createDomain(ObserverLib.MODID);
    private static WorldCacheDomain.SaveKey<StructureMatchingBuffer> STRUCTURE_BUFFER_KEY =
            WORLD_DOMAIN.createSaveKey("structure_buffer", StructureMatchingBuffer::new);

    public static StructureMatchingBuffer getBuffer(World world) {
        return WORLD_DOMAIN.getData(world, STRUCTURE_BUFFER_KEY);
    }

    @Nonnull
    @Override
    public <T extends ChangeObserver> ChangeSubscriber<T> observeArea(World world, BlockPos center, ObserverProvider provider) {
        return getBuffer(world).observeArea(world, center, provider);
    }

    @Override
    public boolean removeObserver(World world, BlockPos pos) {
        return getBuffer(world).removeSubscriber(pos);
    }

    @Nullable
    @Override
    public ChangeSubscriber<? extends ChangeObserver> getSubscriber(World world, BlockPos pos) {
        return getBuffer(world).getSubscriber(pos);
    }

}
