package hellfirepvp.observerlib.client.preview;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.client.StructureRenderWorld;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.util.StructureUtil;
import hellfirepvp.observerlib.client.util.BufferBuilderDecorator;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import hellfirepvp.observerlib.client.util.SimpleBossInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

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

    private DimensionType dimType;
    private BlockPos origin;
    private final StructureSnapshot snapshot;

    private double minimumDisplayDistanceSq = 64;
    private double displayDistanceMultiplier = 1.75;
    private BiPredicate<World, BlockPos> persistenceTest = (world, pos) -> true;

    private ITextComponent barText = null;
    private SimpleBossInfo bossInfo = null;

    private StructurePreview(DimensionType dimType, BlockPos origin, StructureSnapshot snapshot) {
        this.dimType = dimType;
        this.origin = origin;
        this.snapshot = snapshot;
    }

    public static Builder newBuilder(DimensionType sourceWorld, BlockPos source, MatchableStructure structure) {
        return newBuilder(sourceWorld, source, structure, ClientTickHelper.getClientTick());
    }

    public static Builder newBuilder(DimensionType sourceWorld, BlockPos source, MatchableStructure structure, long tick) {
        return new Builder(sourceWorld, source, structure, tick);
    }

    private boolean isInRenderDistance(BlockPos position) {
        double distanceSq = Math.max(this.minimumDisplayDistanceSq, this.snapshot.getStructure().getMaximumOffset().distanceSq(this.snapshot.getStructure().getMinimumOffset()));
        distanceSq *= Math.max(1, displayDistanceMultiplier);
        return this.origin.distanceSq(position) <= distanceSq;
    }

    boolean canRender(World renderWorld, BlockPos renderPosition) {
        if (!this.dimType.equals(renderWorld.getDimension().getType())) {
            return false;
        }
        return this.isInRenderDistance(renderPosition);
    }

    boolean canPersist(World renderWorld, BlockPos position) {
        return this.persistenceTest.test(renderWorld, position);
    }

    public void tick(World renderWorld, BlockPos position) {
        if (this.barText != null) {
            if (this.dimType.equals(renderWorld.getDimension().getType()) && this.isInRenderDistance(position)) {
                if (this.bossInfo == null) {
                    this.bossInfo = SimpleBossInfo.newBuilder(this.barText, BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS).build();
                    this.bossInfo.displayInfo();
                }
                float percFinished = StructureUtil.getMismatches(this.snapshot.getStructure(), renderWorld, this.origin).size() / ((float) this.snapshot.getStructure().getContents().size());
                this.bossInfo.setPercent(percFinished);
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

    void render(World renderWorld, Vec3d playerPos) {
        Optional<Integer> displaySlice = StructureUtil.getLowestMismatchingSlice(this.snapshot.getStructure(), renderWorld, this.origin);
        if (!displaySlice.isPresent()) {
            return; //Nothing to render
        }

        StructureRenderWorld drawWorld = new StructureRenderWorld(this.snapshot.getStructure(), Biomes.PLAINS);
        drawWorld.pushContentFilter(pos -> pos.getY() == displaySlice.get());

        float[] fullBright = new float[] { 15, 15 };
        BlockMismatchColorDecorator colorDecorator = new BlockMismatchColorDecorator();

        Tessellator tes = Tessellator.getInstance();
        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
        BufferBuilderDecorator decorated = BufferBuilderDecorator.decorate(tes.getBuffer());
        decorated.setLightmapDecorator((skyLight, blockLight) -> fullBright);
        decorated.setColorDecorator(colorDecorator);

        GlStateManager.disableAlphaTest();
        GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        GlStateManager.color4f(0.5F, 0.5F, 0.5F, 0.5F);

        GlStateManager.pushMatrix();
        Vec3d vec = new Vec3d(0, 0, 0);
        if (Minecraft.getInstance().gameRenderer != null) {
            vec = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        }
        GlStateManager.translated(-vec.getX(), -vec.getY(), -vec.getZ());

        List<Tuple<BlockPos, ? extends MatchableState>> structureSlice = this.snapshot.getStructure().getStructureSlice(displaySlice.get());
        structureSlice.sort(Comparator.comparingDouble(tpl -> tpl.getA().distanceSq(playerPos.x, playerPos.y, playerPos.z, false)));
        for (Tuple<BlockPos, ? extends MatchableState> expectedBlock : structureSlice) {
            drawWorld.pushContentFilter(pos -> pos.equals(expectedBlock.getA()));

            BlockPos at = expectedBlock.getA().add(this.origin);
            BlockState renderState = expectedBlock.getB().getDescriptiveState(this.snapshot.getSnapshotTick());
            TileEntity renderTile = expectedBlock.getB().createTileEntity(drawWorld, this.snapshot.getSnapshotTick());
            IModelData data = EmptyModelData.INSTANCE;
            if (renderTile != null) {
                data = renderTile.getModelData();
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef(at.getX() + 0.2F, at.getY() + 0.2F, at.getZ() + 0.2F);
            GlStateManager.scalef(0.6F, 0.6F, 0.6F);

            decorated.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            if (!renderWorld.isAirBlock(at)) {
                colorDecorator.isMismatch = true;
            }
            brd.renderBlock(renderState, BlockPos.ZERO, drawWorld, decorated, rand, data);
            colorDecorator.isMismatch = false;
            tes.draw();

            GlStateManager.popMatrix();
            drawWorld.popContentFilter();
        }

        GlStateManager.popMatrix();
        drawWorld.popContentFilter();

        GlStateManager.color4f(1F, 1F, 1F, 1F);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableDepthTest();
        GlStateManager.enableAlphaTest();
    }

    public static class Builder {

        private final StructurePreview preview;

        private Builder(DimensionType dimType, BlockPos origin, MatchableStructure structure, long tick) {
            this.preview = new StructurePreview(dimType, origin, new StructureSnapshot(structure, tick));
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
            this.preview.persistenceTest = this.preview.persistenceTest.and((world, pos) -> this.preview.dimType.equals(world.getDimension().getType()));
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

    private static class BlockMismatchColorDecorator implements BufferBuilderDecorator.ColorDecorator {

        private boolean isMismatch = false;

        @Override
        public int[] decorate(int r, int g, int b, int a) {
            if (this.isMismatch) {
                return new int[] { 255, 0, 0, 128 };
            } else {
                return new int[] { r, g, b, 128 };
            }
        }
    }
}














