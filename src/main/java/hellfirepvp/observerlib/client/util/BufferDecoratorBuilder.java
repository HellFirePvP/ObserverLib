package hellfirepvp.observerlib.client.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexConsumer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: BufferDecoratorBuilder
 * Created by HellFirePvP
 * Date: 11.02.2020 / 18:48
 */
public class BufferDecoratorBuilder {

    private PositionDecorator positionDecorator;
    private ColorDecorator colorDecorator;
    private UVDecorator uvDecorator;
    private IntMapDecorator overlayDecorator;
    private IntMapDecorator lightmapDecorator;
    private NormalDecorator normalDecorator;

    ///////////////////////////////////////////////////////////////////////////
    //      Decoration options
    ///////////////////////////////////////////////////////////////////////////


    public BufferDecoratorBuilder setPositionDecorator(PositionDecorator positionDecorator) {
        this.positionDecorator = positionDecorator;
        return this;
    }

    public BufferDecoratorBuilder setColorDecorator(ColorDecorator colorDecorator) {
        this.colorDecorator = colorDecorator;
        return this;
    }

    public BufferDecoratorBuilder setUvDecorator(UVDecorator uvDecorator) {
        this.uvDecorator = uvDecorator;
        return this;
    }

    public BufferDecoratorBuilder setOverlayDecorator(IntMapDecorator overlayDecorator) {
        this.overlayDecorator = overlayDecorator;
        return this;
    }

    public BufferDecoratorBuilder setLightmapDecorator(IntMapDecorator lightmapDecorator) {
        this.lightmapDecorator = lightmapDecorator;
        return this;
    }

    public BufferDecoratorBuilder setNormalDecorator(NormalDecorator normalDecorator) {
        this.normalDecorator = normalDecorator;
        return this;
    }

    public void decorate(IVertexBuilder builder, Consumer<IVertexBuilder> runDecorated) {
        runDecorated.accept(new DecoratedBuilder(builder, this));
    }

    public void decorate(IVertexConsumer consumer, Consumer<IVertexConsumer> runDecorated) {
        runDecorated.accept(new DecoratedConsumer(consumer, this));
    }

    private static class DecoratedBuilder implements IVertexBuilder {

        final IVertexBuilder vertexBuilder;
        final BufferDecoratorBuilder decorator;

        private DecoratedBuilder(IVertexBuilder vertexBuilder, BufferDecoratorBuilder decorator) {
            this.vertexBuilder = vertexBuilder;
            this.decorator = decorator;
        }

        ///////////////////////////////////////////////////////////////////////////
        //      Methods with decorated changes
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public IVertexBuilder pos(double x, double y, double z) {
            if (this.decorator.positionDecorator != null) {
                double[] newPosition = this.decorator.positionDecorator.decorate(x, y, z);
                return this.vertexBuilder.pos(newPosition[0], newPosition[1], newPosition[2]);
            }
            return this.vertexBuilder.pos(x, y, z);
        }

        @Override
        public IVertexBuilder color(int red, int green, int blue, int alpha) {
            if (this.decorator.colorDecorator != null) {
                int[] newColor = this.decorator.colorDecorator.decorate(red, green, blue, alpha);
                return this.vertexBuilder.color(newColor[0], newColor[1], newColor[2], newColor[3]);
            }
            return this.vertexBuilder.color(red, green, blue, alpha);
        }

        @Override
        public IVertexBuilder tex(float u, float v) {
            if (this.decorator.uvDecorator != null) {
                float[] newUV = this.decorator.uvDecorator.decorate(u, v);
                return this.vertexBuilder.tex(newUV[0], newUV[1]);
            }
            return this.vertexBuilder.tex(u, v);
        }

        @Override
        public IVertexBuilder overlay(int u, int v) {
            if (this.decorator.overlayDecorator != null) {
                int[] newUV = this.decorator.overlayDecorator.decorate(u, v);
                return this.vertexBuilder.overlay(newUV[0], newUV[1]);
            }
            return this.vertexBuilder.overlay(u, v);
        }

        @Override
        public IVertexBuilder lightmap(int u, int v) {
            if (this.decorator.lightmapDecorator != null) {
                int[] newUV = this.decorator.lightmapDecorator.decorate(u, v);
                return this.vertexBuilder.overlay(newUV[0], newUV[1]);
            }
            return this.vertexBuilder.overlay(u, v);
        }

        @Override
        public IVertexBuilder normal(float x, float y, float z) {
            if (this.decorator.normalDecorator != null) {
                float[] newNormals = this.decorator.normalDecorator.decorate(x, y, z);
                return this.vertexBuilder.normal(newNormals[0], newNormals[1], newNormals[2]);
            }
            return this.vertexBuilder.normal(x, y, z);
        }

        @Override
        public void endVertex() {
            this.vertexBuilder.endVertex();
        }
    }

    private static class DecoratedConsumer extends DecoratedBuilder implements IVertexConsumer {

        final IVertexConsumer vertexConsumer;

        private DecoratedConsumer(IVertexConsumer vertexConsumer, BufferDecoratorBuilder decorator) {
            super(vertexConsumer, decorator);
            this.vertexConsumer = vertexConsumer;
        }

        ///////////////////////////////////////////////////////////////////////////
        //      Delegate decorations
        //      At this time we applied decorations ideally
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public VertexFormatElement getCurrentElement() {
            return this.vertexConsumer.getCurrentElement();
        }

        @Override
        public void nextVertexFormatIndex() {
            this.vertexConsumer.nextVertexFormatIndex();
        }

        @Override
        public void putByte(int i, byte b) {
            this.vertexConsumer.putByte(i, b);
        }

        @Override
        public void putShort(int i, short s) {
            this.vertexConsumer.putShort(i, s);
        }

        @Override
        public void putFloat(int i, float f) {
            this.vertexConsumer.putFloat(i, f);
        }

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
