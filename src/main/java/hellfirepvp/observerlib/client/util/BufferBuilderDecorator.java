package hellfirepvp.observerlib.client.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexConsumer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BufferBuilderDecorator
 * Created by HellFirePvP
 * Date: 11.02.2020 / 18:48
 */
public class BufferBuilderDecorator implements IVertexConsumer {

    private final BufferBuilder buffer;

    private PositionDecorator positionDecorator;
    private ColorDecorator colorDecorator;
    private UVDecorator uvDecorator;
    private IntMapDecorator overlayDecorator;
    private IntMapDecorator lightmapDecorator;
    private NormalDecorator normalDecorator;

    private BufferBuilderDecorator(BufferBuilder decorated) {
        this.buffer = decorated;
    }

    public static BufferBuilderDecorator decorate(BufferBuilder buf) {
        return new BufferBuilderDecorator(buf);
    }

    ///////////////////////////////////////////////////////////////////////////
    //      Decoration options
    ///////////////////////////////////////////////////////////////////////////


    public void setPositionDecorator(PositionDecorator positionDecorator) {
        this.positionDecorator = positionDecorator;
    }

    public void setColorDecorator(ColorDecorator colorDecorator) {
        this.colorDecorator = colorDecorator;
    }

    public void setUvDecorator(UVDecorator uvDecorator) {
        this.uvDecorator = uvDecorator;
    }

    public void setOverlayDecorator(IntMapDecorator overlayDecorator) {
        this.overlayDecorator = overlayDecorator;
    }

    public void setLightmapDecorator(IntMapDecorator lightmapDecorator) {
        this.lightmapDecorator = lightmapDecorator;
    }

    public void setNormalDecorator(NormalDecorator normalDecorator) {
        this.normalDecorator = normalDecorator;
    }

    ///////////////////////////////////////////////////////////////////////////
    //      Methods with decorated changes
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public IVertexBuilder pos(double x, double y, double z) {
        if (this.positionDecorator != null) {
            double[] newPosition = this.positionDecorator.decorate(x, y, z);
            return this.buffer.pos(newPosition[0], newPosition[1], newPosition[2]);
        }
        return this.buffer.pos(x, y, z);
    }

    @Override
    public IVertexBuilder color(int red, int green, int blue, int alpha) {
        if (this.colorDecorator != null) {
            int[] newColor = this.colorDecorator.decorate(red, green, blue, alpha);
            return this.buffer.color(newColor[0], newColor[1], newColor[2], newColor[3]);
        }
        return this.buffer.color(red, green, blue, alpha);
    }

    @Override
    public IVertexBuilder tex(float u, float v) {
        if (this.uvDecorator != null) {
            float[] newUV = this.uvDecorator.decorate(u, v);
            return this.buffer.tex(newUV[0], newUV[1]);
        }
        return this.buffer.tex(u, v);
    }

    @Override
    public IVertexBuilder overlay(int u, int v) {
        if (this.overlayDecorator != null) {
            int[] newUV = this.overlayDecorator.decorate(u, v);
            return this.buffer.overlay(newUV[0], newUV[1]);
        }
        return this.buffer.overlay(u, v);
    }

    @Override
    public IVertexBuilder lightmap(int u, int v) {
        if (this.lightmapDecorator != null) {
            int[] newUV = this.lightmapDecorator.decorate(u, v);
            return this.buffer.overlay(newUV[0], newUV[1]);
        }
        return this.buffer.overlay(u, v);
    }

    @Override
    public IVertexBuilder normal(float x, float y, float z) {
        if (this.normalDecorator != null) {
            float[] newNormals = this.normalDecorator.decorate(x, y, z);
            return this.buffer.normal(newNormals[0], newNormals[1], newNormals[2]);
        }
        return this.buffer.normal(x, y, z);
    }

    ///////////////////////////////////////////////////////////////////////////
    //      Delegate decorations
    //      At this time we applied decorations ideally
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void endVertex() {
        this.buffer.endVertex();
    }

    @Override
    public VertexFormatElement getCurrentElement() {
        return this.buffer.getCurrentElement();
    }

    @Override
    public void nextVertexFormatIndex() {
        this.buffer.nextVertexFormatIndex();
    }

    @Override
    public void putByte(int i, byte b) {
        this.buffer.putByte(i, b);
    }

    @Override
    public void putShort(int i, short s) {
        this.buffer.putShort(i, s);
    }

    @Override
    public void putFloat(int i, float f) {
        this.buffer.putFloat(i, f);
    }

    public void begin(int glMode, VertexFormat format) {
        this.buffer.begin(glMode, format);
    }

    ///////////////////////////////////////////////////////////////////////////
    //      Decorator interfaces
    ///////////////////////////////////////////////////////////////////////////

    public static interface PositionDecorator {

        public double[] decorate(double x, double y, double z);

    }

    public static interface NormalDecorator {

        public float[] decorate(float x, float y, float z);

    }

    public static interface IntMapDecorator {

        //0-15 each, return same
        public int[] decorate(int x, int z);

    }

    public static interface UVDecorator {

        public float[] decorate(float u, float v);

    }

    public static interface ColorDecorator {

        public int[] decorate(int r, int g, int b, int a);

    }

}
