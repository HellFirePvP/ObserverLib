package hellfirepvp.observerlib.client.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.client.StructureRenderWorld;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.util.StructureUtil;
import hellfirepvp.observerlib.client.util.*;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructurePreview
 * Created by HellFirePvP
 * Date: 12.02.2020 / 18:43
 */
public class StructurePreview {

    private static final RandomSource rand = RandomSource.create();

    private final ResourceKey<Level> dimension;
    private final BlockPos origin;
    private final StructureSnapshot snapshot;

    private double minimumDisplayDistanceSq = 64;
    private double displayDistanceMultiplier = 1.75;
    private BiPredicate<Level, BlockPos> persistenceTest = (world, pos) -> true;

    private Component barText = null;
    private SimpleBossInfo bossInfo = null;

    private StructurePreview(ResourceKey<Level> dimension, BlockPos origin, StructureSnapshot snapshot) {
        this.dimension = dimension;
        this.origin = origin;
        this.snapshot = snapshot;
    }

    public static Builder newBuilder(ResourceKey<Level> dimension, BlockPos source, MatchableStructure structure) {
        return newBuilder(dimension, source, structure, ClientTickHelper.getClientTick());
    }

    public static Builder newBuilder(ResourceKey<Level> dimension, BlockPos source, MatchableStructure structure, long tick) {
        return new Builder(dimension, source, structure, tick);
    }

    private boolean isInRenderDistance(BlockPos position) {
        double distanceSq = Math.max(this.minimumDisplayDistanceSq, this.snapshot.getStructure().getMaximumOffset().distSqr(this.snapshot.getStructure().getMinimumOffset()));
        distanceSq *= Math.max(1, displayDistanceMultiplier);
        return this.origin.distSqr(position) <= distanceSq;
    }

    boolean canRender(Level renderWorld, BlockPos renderPosition) {
        if (!this.dimension.equals(renderWorld.dimension())) {
            return false;
        }
        return this.isInRenderDistance(renderPosition);
    }

    boolean canPersist(Level renderWorld, BlockPos position) {
        return this.persistenceTest.test(renderWorld, position);
    }

    public void tick(Level renderWorld, BlockPos position) {
        if (this.barText != null) {
            if (this.dimension.equals(renderWorld.dimension()) && this.isInRenderDistance(position)) {
                if (this.bossInfo == null) {
                    this.bossInfo = SimpleBossInfo.create(this.barText, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
                    this.bossInfo.displayInfo();
                }
                float percFinished = StructureUtil.getMismatches(this.snapshot.getStructure(), renderWorld, this.origin).size() / ((float) this.snapshot.getStructure().getContents().size());
                this.bossInfo.setProgress(1F - percFinished);
            } else if (this.bossInfo != null) {
                this.bossInfo.removeInfo();
                this.bossInfo = null;
            }
        }
    }

    void onRemove() {
        if (this.bossInfo != null) {
            this.bossInfo.removeInfo();
        }
    }

    void render(Level renderWorld, PoseStack renderStack, Vec3 playerPos) {
        Optional<Integer> displaySlice = StructureUtil.getLowestMismatchingSlice(this.snapshot.getStructure(), renderWorld, this.origin);
        if (!displaySlice.isPresent()) {
            return; //Nothing to render
        }

        Holder<Biome> plainsBiome = renderWorld.registryAccess().registryOrThrow(ForgeRegistries.Keys.BIOMES).getHolderOrThrow(Biomes.PLAINS);
        StructureRenderWorld drawWorld = new StructureRenderWorld(this.snapshot.getStructure(), plainsBiome);
        drawWorld.pushContentFilter(pos -> pos.getY() == displaySlice.get());

        int[] fullBright = new int[] { 15, 15 };
        BlockMismatchColorDecorator colorDecorator = new BlockMismatchColorDecorator();

        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        BufferDecoratorBuilder decorator = new BufferDecoratorBuilder()
                .setLightmapDecorator((skyLight, blockLight) -> fullBright)
                .setColorDecorator(colorDecorator);

        Runnable transparentSetup = () -> {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        };
        Runnable transparentClean = () -> {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        };

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        renderStack.pushPose();
        renderStack.translate(-camera.x(), -camera.y(), -camera.z());

        List<Tuple<BlockPos, ? extends MatchableState>> structureSlice = this.snapshot.getStructure().getStructureSlice(displaySlice.get());
        structureSlice.sort(Comparator.comparingDouble(tpl -> tpl.getA().distToLowCornerSqr(playerPos.x, playerPos.y, playerPos.z)));
        Collections.reverse(structureSlice);
        for (Tuple<BlockPos, ? extends MatchableState> expectedBlock : structureSlice) {
            BlockPos at = expectedBlock.getA().offset(this.origin);
            BlockEntity renderTile = expectedBlock.getB().createTileEntity(drawWorld, at, this.snapshot.getSnapshotTick());
            BlockState actual = renderWorld.getBlockState(at);

            if (this.snapshot.getStructure().matchesSingleBlock(renderWorld, this.origin, expectedBlock.getA(), actual, renderWorld.getBlockEntity(at))) {
                continue;
            }
            BlockState renderState;
            if (expectedBlock.getB() == MatchableState.REQUIRES_AIR) {
                renderState = Blocks.WHITE_WOOL.defaultBlockState(); //choosing a very visible blockstate for required air
            } else {
                renderState = expectedBlock.getB().getDescriptiveState(this.snapshot.getSnapshotTick());
            }

            ModelData data = renderTile != null ? renderTile.getModelData() : ModelData.EMPTY;

            renderStack.pushPose();
            renderStack.translate(at.getX() + 0.2F, at.getY() + 0.2F, at.getZ() + 0.2F);
            renderStack.scale(0.6F, 0.6F, 0.6F);

            if (!actual.isAir()) {
                colorDecorator.isMismatch = true;
            }
            drawWorld.pushContentFilter(pos -> pos.equals(expectedBlock.getA()));
            if (!renderState.getFluidState().isEmpty()) {
                RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(RenderType.translucent(), transparentSetup, transparentClean);
                decorator.decorate(buffers.getBuffer(decorated), buf -> {
                    brd.renderLiquid(BlockPos.ZERO, drawWorld, buf, renderState, renderState.getFluidState());
                });
            }

            RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(ItemBlockRenderTypes.getMovingBlockRenderType(renderState), transparentSetup, transparentClean);
            decorator.decorate(buffers.getBuffer(decorated), buf -> {
                brd.renderBatched(renderState, BlockPos.ZERO, drawWorld, renderStack, buf, true, rand, data, decorated, false);
            });
            buffers.endBatch();

            drawWorld.popContentFilter();
            colorDecorator.isMismatch = false;

            renderStack.popPose();
        }

        drawWorld.popContentFilter();

        renderStack.popPose();
    }

    public static class Builder {

        private final StructurePreview preview;

        private Builder(ResourceKey<Level> dimension, BlockPos origin, MatchableStructure structure, long tick) {
            this.preview = new StructurePreview(dimension, origin, new StructureSnapshot(structure, tick));
        }

        public Builder setMinimumDisplayDistance(double minimumDisplayDistance) {
            this.preview.minimumDisplayDistanceSq = minimumDisplayDistance * minimumDisplayDistance;
            return this;
        }

        public Builder setDisplayDistanceMultiplier(double displayDistanceMultiplier) {
            this.preview.displayDistanceMultiplier = displayDistanceMultiplier;
            return this;
        }

        public Builder removeIfOutOfRenderDistance() {
            this.preview.persistenceTest = this.preview.persistenceTest.and((world, pos) -> this.preview.isInRenderDistance(pos));
            return this;
        }

        public Builder removeIfOutInDifferentWorld() {
            this.preview.persistenceTest = this.preview.persistenceTest.and((world, pos) -> this.preview.dimension.equals(world.dimension()));
            return this;
        }

        public Builder andPersistOnlyIf(BiPredicate<Level, BlockPos> test) {
            this.preview.persistenceTest = this.preview.persistenceTest.and(test);
            return this;
        }

        public Builder showBar(Component headline) {
            this.preview.barText = headline;
            return this;
        }

        public StructurePreview buildAndSet() {
            StructurePreviewHandler.getInstance().setStructurePreview(this.preview);
            return this.preview;
        }
    }

    private static class BlockMismatchColorDecorator implements BufferDecoratorBuilder.ColorDecorator {

        private static final int[] errorColor = new int[] { 255, 0, 0, 128 };

        private boolean isMismatch = false;

        @Override
        public int[] decorate(int r, int g, int b, int a) {
            if (this.isMismatch) {
                return errorColor;
            } else {
                return new int[] { r, g, b, 128 };
            }
        }
    }
}














