package hellfirepvp.observerlib.api.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Exemplary implementation of {@link MatchableTile<T>} with tiles of a specific Type and functional
 * implementations of writing display and post placement data.
 *
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleMatchableTileEntity
 * Created by HellFirePvP
 * Date: 11.08.2019 / 09:39
 */
public class SimpleMatchableTileEntity<T extends TileEntity> implements MatchableTile<T> {

    private final TileEntityType<T> tileType;
    private final BiConsumer<T, CompoundNBT> writeDisplayData;
    private final Consumer<T> writePlacement;

    public SimpleMatchableTileEntity(TileEntityType<T> tileType, BiConsumer<T, CompoundNBT> writeDisplayData, Consumer<T> writePlacement) {
        this.tileType = tileType;
        this.writeDisplayData = writeDisplayData;
        this.writePlacement = writePlacement;
    }

    @Override
    public void writeDisplayData(@Nonnull T tile, long tick, @Nonnull CompoundNBT tag) {
        this.writeDisplayData.accept(tile, tag);
    }

    @Override
    public void postPlacement(@Nonnull T tile, @Nonnull IBlockReader world, BlockPos pos) {
        this.writePlacement.accept(tile);
    }

    @Override
    public boolean matches(@Nullable IBlockReader reader, @Nonnull BlockPos absolutePosition, @Nonnull T tile) {
        return tile.getType().equals(this.tileType);
    }
}
