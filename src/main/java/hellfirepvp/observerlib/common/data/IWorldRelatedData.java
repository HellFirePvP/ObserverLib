package hellfirepvp.observerlib.common.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: IWorldRelatedData
 * Created by HellFirePvP
 * Date: 12.08.2016 / 11:33
 */
public interface IWorldRelatedData<T extends IWorldRelatedData<T>> {

    WorldCacheDomain.SaveKey<T> getSaveKey();

    void markSaved();

    default void onLoad(Level world) {}

    default void setLoader(FileLoader<?> loader) {}

    void writeAdditionalData(File saveDir, File backupDir) throws IOException;

    void readAdditionalData(File directory, FileLoader<?> fileLoader);

    interface FileResolver {
        File resolveFile(File directory);
    }

    interface FileLoader<F> {
        Optional<Tuple<F, File>> loadData(FileResolver resolver, Codec<F> codec);
    }
}
