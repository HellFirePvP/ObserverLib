package hellfirepvp.observerlib.common.data.io;

import java.io.File;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: DirectorySet
 * Created by HellFirePvP
 * Date: 03.06.2020 / 19:00
 */
public class DirectorySet {

    private final File actualDirectory;
    private final File backupDirectory;

    DirectorySet(File dataDirectory) {
        this.actualDirectory = dataDirectory;
        this.backupDirectory = new File(dataDirectory.getParent(), dataDirectory.getName() + "_backup");
    }

    public File getParentDirectory() {
        return getActualDirectory().getParentFile();
    }

    public File getActualDirectory() {
        return this.getActualDirectory(false);
    }

    public File getActualDirectory(boolean create) {
        if (create && !this.actualDirectory.exists()) {
            this.actualDirectory.mkdirs();
        }
        return this.actualDirectory;
    }

    public File getBackupDirectory() {
        return this.getBackupDirectory(false);
    }

    public File getBackupDirectory(boolean create) {
        if (create && !this.backupDirectory.exists()) {
            this.backupDirectory.mkdirs();
        }
        return this.backupDirectory;
    }

    public DirectorySet getErrorDirectories() {
        File errorDirectory = new File(actualDirectory.getParent(), actualDirectory.getName() + "_error");
        if (!errorDirectory.exists()) {
            errorDirectory.mkdirs();
        }
        return new DirectorySet(errorDirectory);
    }
}
