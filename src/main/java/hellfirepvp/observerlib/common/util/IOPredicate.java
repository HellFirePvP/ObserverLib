package hellfirepvp.observerlib.common.util;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: IOPredicate
 * Created by HellFirePvP
 * Date: 22.08.2019 / 19:35
 */
public interface IOPredicate<T> {

    boolean testUnsafe(T t) throws IOException;

}
