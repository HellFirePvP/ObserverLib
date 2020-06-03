package hellfirepvp.observerlib.common.api;

import hellfirepvp.observerlib.ObserverLib;
import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.api.util.FutureCallback;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.WorldCacheManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

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

    public static void getBuffer(IWorld world, Consumer<StructureMatchingBuffer> onLoad) {
        WORLD_DOMAIN.getData(world, STRUCTURE_BUFFER_KEY, onLoad);
    }

    @Override
    public <T extends ChangeObserver> void observeArea(World world, BlockPos center, ObserverProvider provider, FutureCallback<ChangeSubscriber<T>> callback) {
        getBuffer(world, buf -> buf.observeArea(world, center, provider, callback));
    }

    @Override
    public void removeObserver(World world, BlockPos pos, FutureCallback<Boolean> callback) {
        getBuffer(world, buf -> buf.removeSubscriber(pos, callback));
    }

    @Override
    public void getSubscriber(World world, BlockPos pos, FutureCallback<ChangeSubscriber<? extends ChangeObserver>> callback) {
        getBuffer(world, buf -> buf.getSubscriber(pos, callback));
    }
}
