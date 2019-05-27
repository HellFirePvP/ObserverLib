package hellfirepvp.observerlib.common.api;

import hellfirepvp.observerlib.api.ChangeObserver;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.api.structure.ObserverProvider;
import hellfirepvp.observerlib.common.data.MatcherDataManager;
import hellfirepvp.observerlib.common.data.StructureMatchingBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

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

    private StructureMatchingBuffer getBuffer(IWorld world) {
        return MatcherDataManager.getOrLoadData(world);
    }

    @Nonnull
    @Override
    public <T extends ChangeObserver> ChangeSubscriber<T> observeArea(IWorld world, BlockPos center, ObserverProvider provider) {
        return getBuffer(world).observeArea(world, center, provider);
    }

    @Override
    public boolean removeObserver(IWorld world, BlockPos pos) {
        return getBuffer(world).removeSubscriber(pos);
    }

    @Nullable
    @Override
    public ChangeSubscriber<? extends ChangeObserver> getSubscriber(IWorld world, BlockPos pos) {
        return getBuffer(world).getSubscriber(pos);
    }

}
