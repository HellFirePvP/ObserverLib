package hellfirepvp.observerlib.common.data;

import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: IWorldRelatedData
 * Created by HellFirePvP
 * Date: 12.08.2016 / 11:33
 */
public interface IWorldRelatedData<T extends IWorldRelatedData<T>> {

    public WorldCacheDomain.SaveKey<T> getSaveKey();

    public abstract void markSaved();

    default public void onLoad(Level world) {}

    public void writeAdditionalData(File saveDir, File backupDir) throws IOException;

    public void readAdditionalData(File dir) throws IOException;

}
