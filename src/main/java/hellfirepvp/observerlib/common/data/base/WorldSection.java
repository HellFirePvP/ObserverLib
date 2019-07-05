package hellfirepvp.observerlib.common.data.base;

import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldSection
 * Created by HellFirePvP
 * Date: 29.05.2019 / 22:59
 */
public abstract class WorldSection {

    private final int sX, sZ;

    protected WorldSection(int sX, int sZ) {
        this.sX = sX;
        this.sZ = sZ;
    }

    public final int getSectionX() {
        return sX;
    }

    public final int getSectionZ() {
        return sZ;
    }

    public abstract void writeToNBT(CompoundNBT tag);

    public abstract void readFromNBT(CompoundNBT tag);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldSection section = (WorldSection) o;
        return sX == section.sX && sZ == section.sZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sX, sZ);
    }

}
