package hellfirepvp.observerlib.client.preview;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.client.StructureRenderWorld;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.util.StructureUtil;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import hellfirepvp.observerlib.client.util.RenderTypeDecorator;
import hellfirepvp.observerlib.client.util.SimpleBossInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

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

    private static final Random rand = new Random();

    private RegistryKey<World> dimension;
    private BlockPos origin;
    private final StructureSnapshot snapshot;

    private double minimumDisplayDistanceSq = 64;
    private double displayDistanceMultiplier = 1.75;
    private BiPredicate<World, BlockPos> persistenceTest = (world, pos) -> true;

    private ITextComponent barText = null;
    private SimpleBossInfo bossInfo = null;

    private StructurePreview(RegistryKey<World> dimension, BlockPos origin, StructureSnapshot snapshot) {
        this.dimension = dimension;
        this.origin = origin;
        this.snapshot = snapshot;
    }

    public static Builder newBuilder(RegistryKey<World> dimension, BlockPos source, MatchableStructure structure) {
        return newBuilder(dimension, source, structure, ClientTickHelper.getClientTick());
    }

    public static Builder newBuilder(RegistryKey<World> dimension, BlockPos source, MatchableStructure structure, long tick) {
        return new Builder(dimension, source, structure, tick);
    }

    private boolean isInRenderDistance(BlockPos position) {
        double distanceSq = Math.max(this.minimumDisplayDistanceSq, this.snapshot.getStructure().getMaximumOffset().distanceSq(this.snapshot.getStructure().getMinimumOffset()));
        distanceSq *= Math.max(1, displayDistanceMultiplier);
        return this.origin.distanceSq(position) <= distanceSq;
    }

    boolean canRender(World renderWorld, BlockPos renderPosition) {
        if (!this.dimension.equals(renderWorld.func_234923_W_())) {
            return false;
        }
        return this.isInRenderDistance(renderPosition);
    }

    boolean canPersist(World renderWorld, BlockPos position) {
        return this.persistenceTest.test(renderWorld, position);
    }

    public void tick(World renderWorld, BlockPos position) {
        if (this.barText != null) {
            if (this.dimension.equals(renderWorld.func_234923_W_()) && this.isInRenderDistance(position)) {
                if (this.bossInfo == null) {
                    this.bossInfo = SimpleBossInfo.newBuilder(this.barText, BossInfo.Color.WHITE, BossInfo.Overlay.PROGRESS).build();
                    this.bossInfo.displayInfo();
                }
                float percFinished = StructureUtil.getMismatches(this.snapshot.getStructure(), renderWorld, this.origin).size() / ((float) this.snapshot.getStructure().getContents().size());
                this.bossInfo.setPercent(1F - percFinished);
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

    void render(World renderWorld, MatrixStack renderStack, Vector3d playerPos) {
        Optional<Integer> displaySlice = StructureUtil.getLowestMismatchingSlice(this.snapshot.getStructure(), renderWorld, this.origin);
        if (!displaySlice.isPresent()) {
            return; //Nothing to render
        }

        Biome plainsBiome = renderWorld.func_241828_r().func_243612_b(Registry.BIOME_KEY).getValueForKey(Biomes.PLAINS);
        StructureRenderWorld drawWorld = new StructureRenderWorld(this.snapshot.getStructure(), plainsBiome);
        drawWorld.pushContentFilter(pos -> pos.getY() == displaySlice.get());

        int[] fullBright = new int[] { 15, 15 };
        BlockMismatchColorDecorator colorDecorator = new BlockMismatchColorDecorator();

        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
        IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        BufferDecoratorBuilder decorator = new BufferDecoratorBuilder()
                .setLightmapDecorator((skyLight, blockLight) -> fullBright)
                .setColorDecorator(colorDecorator);

        Runnable transparentSetup = () -> {
            RenderSystem.disableAlphaTest();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        };
        Runnable transparentClean = () -> {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.enableAlphaTest();
        };

        Vector3d vec = new Vector3d(0, 0, 0);
        if (Minecraft.getInstance().gameRenderer != null) {
            vec = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        }

        renderStack.push();
        renderStack.translate(-vec.getX(), -vec.getY(), -vec.getZ());

        List<Tuple<BlockPos, ? extends MatchableState>> structureSlice = this.snapshot.getStructure().getStructureSlice(displaySlice.get());
        structureSlice.sort(Comparator.comparingDouble(tpl -> tpl.getA().distanceSq(playerPos.x, playerPos.y, playerPos.z, false)));
        Collections.reverse(structureSlice);
        for (Tuple<BlockPos, ? extends MatchableState> expectedBlock : structureSlice) {
            BlockPos at = expectedBlock.getA().add(this.origin);
            BlockState renderState = expectedBlock.getB().getDescriptiveState(this.snapshot.getSnapshotTick());
            TileEntity renderTile = expectedBlock.getB().createTileEntity(drawWorld, this.snapshot.getSnapshotTick());
            BlockState actual = renderWorld.getBlockState(at);

            if (this.snapshot.getStructure().matchesSingleBlock(renderWorld, this.origin, expectedBlock.getA(), actual, renderWorld.getTileEntity(at))) {
                continue;
            }

            IModelData data = renderTile != null ? renderTile.getModelData() : EmptyModelData.INSTANCE;

            renderStack.push();
            renderStack.translate(at.getX() + 0.2F, at.getY() + 0.2F, at.getZ() + 0.2F);
            renderStack.scale(0.6F, 0.6F, 0.6F);

            if (!actual.isAir(renderWorld, at)) {
                colorDecorator.isMismatch = true;
            }
            drawWorld.pushContentFilter(pos -> pos.equals(expectedBlock.getA()));
            if (!renderState.getFluidState().isEmpty()) {
                RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(RenderType.getTranslucent(), transparentSetup, transparentClean);
                decorator.decorate(buffers.getBuffer(decorated), buf -> {
                    brd.renderFluid(BlockPos.ZERO, drawWorld, buf, renderState.getFluidState());
                });
            }

            RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(RenderTypeLookup.func_239221_b_(renderState), transparentSetup, transparentClean);
            decorator.decorate(buffers.getBuffer(decorated), buf -> {
                brd.renderModel(renderState, BlockPos.ZERO, drawWorld, renderStack, buf, true, rand, data);
            });
            buffers.finish();

            drawWorld.popContentFilter();
            colorDecorator.isMismatch = false;

            renderStack.pop();
        }

        drawWorld.popContentFilter();

        renderStack.pop();
    }

    public static class Builder {

        private final StructurePreview preview;

        private Builder(RegistryKey<World> dimension, BlockPos origin, MatchableStructure structure, long tick) {
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
            this.preview.persistenceTest = this.preview.persistenceTest.and((world, pos) -> this.preview.dimension.equals(world.func_234923_W_()));
            return this;
        }

        public Builder andPersistOnlyIf(BiPredicate<World, BlockPos> test) {
            this.preview.persistenceTest = this.preview.persistenceTest.and(test);
            return this;
        }

        public Builder showBar(ITextComponent headline) {
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














