package hellfirepvp.observerlib.api.client;

import com.mojang.blaze3d.platform.GlStateManager;
import hellfirepvp.observerlib.api.structure.Structure;
import hellfirepvp.observerlib.client.util.ClientTickHelper;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
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

    public StructureRenderer(Structure structure) {
        this.structure = structure;
        this.world = new StructureRenderWorld(this.structure, Biomes.PLAINS);
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

    public void render3DGUI(double x, double y, float pTicks) {
        render3DSliceGUI(x, y, pTicks, Optional.empty());
    }

    public void render3DSliceGUI(double x, double y, float pTicks, Optional<Integer> slice) {
        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (currentScreen == null) {
            return;
        }

        MainWindow window = Minecraft.getInstance().mainWindow;
        double scale = window.getGuiScaleFactor();

        double mul = 10.5;
        double size = 2;
        double minSize = 0.5;

        Vec3i max = this.structure.getMaximumOffset();//Ja.. ne IDE is nur so gut wie der entwickler/in davor :P
        Vec3i min = this.structure.getMinimumOffset();

        double maxLength = 0;
        double pointDst = max.getX() - min.getX();
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
            size = (size - minSize) * (1D - (maxLength / 20D));
        }

        double dr = -5.75 * size;

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        slice.ifPresent(ySlice -> this.world.pushContentFilter((pos) -> pos.getY() == ySlice));

        GlStateManager.translated(x + 16D / scale, y + 16D / scale, 512);
        GlStateManager.translated(dr, dr, dr);
        GlStateManager.rotatef((float) rotationX, 1, 0, 0);
        GlStateManager.rotatef((float) rotationY, 0, 1, 0);
        GlStateManager.rotatef((float) rotationZ, 0, 0, 1);
        GlStateManager.translated(-dr, -dr, -dr);
        GlStateManager.scaled(-size * mul, -size * mul, -size * mul);
        slice.ifPresent(ySlice -> GlStateManager.scaled(0, -ySlice, 0));

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        this.structure.getContents().keySet()
                .forEach(pos -> {
                    if (this.isolateIndividualBlockRender) {
                        this.world.pushContentFilter(wPos -> wPos.equals(pos));
                    }
                    BlockState view = this.world.getBlockState(pos);
                    if (!view.getBlock().equals(Blocks.AIR)) {
                        this.renderBlockSafely(pos, view, buf);
                    }
                    if (this.isolateIndividualBlockRender) {
                        this.world.popContentFilter();
                    }
                });
        tes.draw();

        this.structure.getContents().keySet()
                .forEach(pos -> {
                    if (this.isolateIndividualBlockRender) {
                        this.world.pushContentFilter(wPos -> wPos.equals(pos));
                    }
                    TileEntity tile = this.world.getTileEntity(pos);
                    if (tile != null) {
                        TileEntityRenderer tesr = TileEntityRendererDispatcher.instance.getRenderer(tile);
                        if (tesr != null) {
                            tesr.render(tile, pos.getX(), pos.getY(), pos.getZ(), pTicks, -1);
                        }
                    }
                    if (this.isolateIndividualBlockRender) {
                        this.world.popContentFilter();
                    }
                });

        slice.ifPresent(ySlice -> this.world.popContentFilter());
        GlStateManager.popMatrix();
    }

    private void renderBlockSafely(BlockPos offset, BlockState state, BufferBuilder vb) {
        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
        try {
            brd.renderBlock(state, offset, this.world, vb, rand, EmptyModelData.INSTANCE);
        } catch (Exception exc) {
            BlockRenderType type = state.getRenderType();
            if (type == BlockRenderType.MODEL) {
                IBakedModel model = brd.getModelForState(state);
                long posRandom = state.getPositionRandom(offset);
                brd.getBlockModelRenderer().renderModel(this.world, model, state, offset, vb, true, rand, posRandom, EmptyModelData.INSTANCE);
            }
        }
    }
}
