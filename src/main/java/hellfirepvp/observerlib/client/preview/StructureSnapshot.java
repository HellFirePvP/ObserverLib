package hellfirepvp.observerlib.client.preview;

import hellfirepvp.observerlib.api.structure.MatchableStructure;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureSnapshot
 * Created by HellFirePvP
 * Date: 12.02.2020 / 18:46
 */
public class StructureSnapshot {

    private final MatchableStructure structure;
    private final long snapshot;

    StructureSnapshot(MatchableStructure structure, long snapshot) {
        this.structure = structure;
        this.snapshot = snapshot;
    }

    MatchableStructure getStructure() {
        return structure;
    }

    long getSnapshotTick() {
        return snapshot;
    }
}
