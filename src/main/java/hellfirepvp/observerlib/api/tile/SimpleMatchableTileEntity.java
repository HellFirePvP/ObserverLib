package hellfirepvp.observerlib.api.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

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
public class SimpleMatchableTileEntity<T extends BlockEntity> implements MatchableTile<T> {

    private final BlockEntityType<T> tileType;
    private final BiConsumer<T, CompoundTag> writeDisplayData;
    private final Consumer<T> writePlacement;

    public SimpleMatchableTileEntity(BlockEntityType<T> tileType, BiConsumer<T, CompoundTag> writeDisplayData, Consumer<T> writePlacement) {
        this.tileType = tileType;
        this.writeDisplayData = writeDisplayData;
        this.writePlacement = writePlacement;
    }

    @Override
    public void writeDisplayData(@Nonnull T tile, long tick, @Nonnull CompoundTag tag) {
        this.writeDisplayData.accept(tile, tag);
    }

    @Override
    public void postPlacement(@Nonnull T tile, @Nonnull BlockGetter world, BlockPos pos) {
        this.writePlacement.accept(tile);
    }

    @Override
    public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull T tile) {
        return tile.getType().equals(this.tileType);
    }
}
