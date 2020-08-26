package hellfirepvp.observerlib.api;

import hellfirepvp.observerlib.api.block.BlockChangeSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * The core centerpiece of ObserverLib.
 *
 * A subscriber implementation of the ChangeObserver, caching validity of the observer's state.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: ChangeSubscriber
 * Created by HellFirePvP
 * Date: 26.04.2019 / 22:31
 */
public interface ChangeSubscriber<T extends ChangeObserver> {

    /**
     * The current observer this subscriber is forwarding relevant changes to.
     *
     * @return this observer
     */
    @Nonnull
    public T getObserver();

    /**
     * The current change set to be used by the ChangeObserver to determine validity of the current state.
     *
     * @return the current ChangeSet, read-only.
     */
    @Nonnull
    public BlockChangeSet getCurrentChangeSet();

    /**
     * Test if the observer determines the current state valid or not.
     *
     * For example, for structures this would return true if the observer determines the structure to be
     * valid at the current state of the world, false if any part is mismatching.
     *
     * What "valid" means in this context is up to the ChangeObserver's
     * {@link ChangeObserver#notifyChange(World, BlockPos, BlockChangeSet)}.
     * This method is ONLY CALLED IF it has never been called ever since observations have started for this Observer, OR
     * if the current {@link BlockChangeSet} for the subscriber is not empty and needs to be processed by the Observer.
     * The result of that is CACHED, until there is any change.
     *
     * @param world the world to test in
     *
     * @return true, if the current state is valid, false if not.
     */
    public boolean isValid(World world);

}
