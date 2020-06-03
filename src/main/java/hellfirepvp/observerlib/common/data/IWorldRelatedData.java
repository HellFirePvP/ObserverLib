package hellfirepvp.observerlib.common.data;

import hellfirepvp.observerlib.common.data.io.DirectorySet;

import java.io.File;
import java.io.IOException;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: IWorldRelatedData
 * Created by HellFirePvP
 * Date: 12.08.2016 / 11:33
 */
public interface IWorldRelatedData {

    public WorldCacheDomain.SaveKey getSaveKey();

    public DirectorySet getDirectory();

    public void markSaved();

    public void writeData() throws IOException;

    public void readData() throws IOException;

}
