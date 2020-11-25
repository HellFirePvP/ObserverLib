package hellfirepvp.observerlib.api.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.client.util.RenderTypeDecorator;
import hellfirepvp.observerlib.common.block.BlockAirRequirement;
import hellfirepvp.observerlib.common.util.RegistryUtil;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Optional;
import java.util.Random;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureRenderer
 * Created by HellFirePvP
 * Date: 08.10.2019 / 19:50
 */
public class StructureRenderer {

    private static final Random rand = new Random();

    private final StructureRenderWorld world;
    private final Structure structure;

    //In degrees
    private double rotationX = 0, rotationY = 0, rotationZ = 0;
    private boolean isolateIndividualBlockRender = false;

    private boolean displayWithRequiredAir = false;

    public StructureRenderer(Structure structure) {
        this.structure = structure;
        Biome plainsBiome = RegistryUtil.client().getValue(Registry.BIOME_KEY, Biomes.PLAINS);
        this.world = new StructureRenderWorld(this.structure, plainsBiome);
        this.resetRotation();
    }

    private void resetRotation() {
        this.rotationX = -30;
        this.rotationY = 45;
        this.rotationZ = 0;
    }

    public void rotate(double x, double y, double z) {
        this.rotationX += x;
        this.rotationY += y;
        this.rotationZ += z;
    }

    public void setRenderWithRequiredAir(boolean displayWithRequiredAir) {
        this.displayWithRequiredAir = displayWithRequiredAir;
    }

    public StructureRenderer setIsolateIndividualBlock(boolean isolateIndividualBlockRender) {
        this.isolateIndividualBlockRender = isolateIndividualBlockRender;
        return this;
    }

    public void rotateFromMouseDrag(float mouseDX, float mouseDZ) {
        this.rotate(0.5 * -mouseDZ, 0.5 * mouseDX, 0);
    }

    public IWorldReader getRenderWorld() {
        return world;
    }

    public Structure getStructure() {
        return structure;
    }

    public int getDefaultSlice() {
        return this.structure.getMinimumOffset().getY();
    }

    public boolean hasSlice(int y) {
        return y >= this.structure.getMinimumOffset().getY() && y <= this.structure.getMaximumOffset().getY();
    }

    public void render3DGUI(MatrixStack renderStack, double x, double y, float pTicks) {
        render3DSliceGUI(renderStack, x, y, pTicks, Optional.empty());
    }

    public void render3DSliceGUI(MatrixStack renderStack, double x, double y, float pTicks, Optional<Integer> slice) {
        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (currentScreen == null) {
            return;
        }

        MainWindow window = Minecraft.getInstance().getMainWindow();
        double scale = window.getGuiScaleFactor();

        float mul = 10.5F;
        float size = 2;
        float minSize = 0.5F;

        Vector3i max = this.structure.getMaximumOffset();
        Vector3i min = this.structure.getMinimumOffset();

        float maxLength = 0;
        float pointDst = max.getX() - min.getX();
        if (pointDst > maxLength) {
            maxLength = pointDst;
        }
        pointDst = max.getY() - min.getY();
        if (pointDst > maxLength) {
            maxLength = pointDst;
        }
        pointDst = max.getZ() - min.getZ();
        if (pointDst > maxLength) {
            maxLength = pointDst;
        }
        maxLength -= 5;

        if(maxLength > 0) {
            size = (size - minSize) * (1F - (maxLength / 20F));
        }

        float dr = -5.75F * size;

        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

        slice.ifPresent(ySlice -> this.world.pushContentFilter((pos) -> pos.getY() == ySlice));

        renderStack.push();
        renderStack.translate(x + 16D / scale, y + 16D / scale, 512);
        renderStack.translate(dr, dr, dr);
        renderStack.rotate(Vector3f.XP.rotationDegrees((float) rotationX));
        renderStack.rotate(Vector3f.YP.rotationDegrees((float) rotationY));
        renderStack.rotate(Vector3f.ZP.rotationDegrees((float) rotationZ));
        renderStack.translate(-dr, -dr, -dr);
        renderStack.scale(-size * mul, -size * mul, -size * mul);
        slice.ifPresent(ySlice -> renderStack.translate(0, -ySlice, 0));

        BlockAirRequirement.displayRequiredAir = this.displayWithRequiredAir;

        this.structure.getContents().keySet()
                .forEach(pos -> {
                    BlockState view = this.world.getBlockState(pos);
                    if (!view.getBlock().equals(Blocks.AIR)) {
                        renderStack.push();
                        renderStack.translate(pos.getX(), pos.getY(), pos.getZ());

                        if (!view.getFluidState().isEmpty()) {
                            this.renderFluid(pos, view.getFluidState(), buffers.getBuffer(wrapBlockRenderType(RenderType.getTranslucent())));
                        }
                        RenderType type = wrapBlockRenderType(RenderTypeLookup.func_239221_b_(view));
                        if (this.isolateIndividualBlockRender) {
                            this.world.pushContentFilter(wPos -> wPos.equals(pos));
                            this.renderBlock(pos, view, buffers.getBuffer(type), renderStack);
                            this.world.popContentFilter();
                        } else {
                            this.renderBlock(pos, view, buffers.getBuffer(type), renderStack);
                        }
                        renderStack.pop();
                    }
                });
        buffers.finish();

        this.structure.getContents().keySet()
                .forEach(pos -> {
                    if (this.isolateIndividualBlockRender) {
                        this.world.pushContentFilter(wPos -> wPos.equals(pos));
                    }
                    TileEntity tile = this.world.getTileEntity(pos);
                    if (tile != null) {
                        TileEntityRenderer tesr = TileEntityRendererDispatcher.instance.getRenderer(tile);
                        if (tesr != null) {
                            renderStack.push();
                            renderStack.translate(pos.getX(), pos.getY(), pos.getZ());
                            tesr.render(tile, 0, renderStack, buffers, WorldRenderer.getCombinedLight(this.world, pos), OverlayTexture.NO_OVERLAY);
                            renderStack.pop();
                        }
                    }
                    if (this.isolateIndividualBlockRender) {
                        this.world.popContentFilter();
                    }
                });
        buffers.finish();

        if (this.displayWithRequiredAir) {
            BlockAirRequirement.displayRequiredAir = false;
        }

        slice.ifPresent(ySlice -> this.world.popContentFilter());
        renderStack.pop();
    }

    private RenderType wrapBlockRenderType(RenderType type) {
        return RenderTypeDecorator.wrapSetup(type, RenderSystem::disableLighting, () -> {});
    }

    private void renderFluid(BlockPos pos, FluidState fluidState, IVertexBuilder buf) {
        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
        brd.renderFluid(pos, this.world, buf, fluidState);
    }

    private void renderBlock(BlockPos offset, BlockState state, IVertexBuilder vb, MatrixStack renderStack) {
        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
        try {
            //random is given position-based seed later on
            brd.renderModel(state, offset, this.world, renderStack, vb, false, rand, EmptyModelData.INSTANCE);
        } catch (Exception exc) {
            BlockRenderType type = state.getRenderType();
            if (type == BlockRenderType.MODEL) {
                IBakedModel model = brd.getModelForState(state);
                long posRandom = state.getPositionRandom(offset);
                brd.getBlockModelRenderer().renderModel(this.world, model, state, offset, renderStack, vb, true, rand, posRandom, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
            }
        }
    }
}
