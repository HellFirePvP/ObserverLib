package hellfirepvp.observerlib.client.util;

import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.Nullable;

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

    public static BufferDecoratorBuilder withPosition(PositionDecorator positionDecorator) {
        return new BufferDecoratorBuilder().setPositionDecorator(positionDecorator);
    }

    public BufferDecoratorBuilder setPositionDecorator(PositionDecorator positionDecorator) {
        this.positionDecorator = positionDecorator;
        return this;
    }

    public static BufferDecoratorBuilder withColor(ColorDecorator colorDecorator) {
        return new BufferDecoratorBuilder().setColorDecorator(colorDecorator);
    }

    public BufferDecoratorBuilder setColorDecorator(ColorDecorator colorDecorator) {
        this.colorDecorator = colorDecorator;
        return this;
    }

    public static BufferDecoratorBuilder withUV(UVDecorator uvDecorator) {
        return new BufferDecoratorBuilder().setUvDecorator(uvDecorator);
    }

    public BufferDecoratorBuilder setUvDecorator(UVDecorator uvDecorator) {
        this.uvDecorator = uvDecorator;
        return this;
    }

    public static BufferDecoratorBuilder withOverly(IntMapDecorator overlayDecorator) {
        return new BufferDecoratorBuilder().setOverlayDecorator(overlayDecorator);
    }

    public BufferDecoratorBuilder setOverlayDecorator(IntMapDecorator overlayDecorator) {
        this.overlayDecorator = overlayDecorator;
        return this;
    }

    public static BufferDecoratorBuilder withLightmap(IntMapDecorator lightmapDecorator) {
        return new BufferDecoratorBuilder().setLightmapDecorator(lightmapDecorator);
    }

    public BufferDecoratorBuilder setLightmapDecorator(IntMapDecorator lightmapDecorator) {
        this.lightmapDecorator = lightmapDecorator;
        return this;
    }

    public static BufferDecoratorBuilder withNormal(NormalDecorator normalDecorator) {
        return new BufferDecoratorBuilder().setNormalDecorator(normalDecorator);
    }

    public BufferDecoratorBuilder setNormalDecorator(NormalDecorator normalDecorator) {
        this.normalDecorator = normalDecorator;
        return this;
    }

    public void decorate(VertexConsumer builder, Consumer<VertexConsumer> runDecorated) {
        runDecorated.accept(new DecoratedBuilder(builder, this));
    }

    public void decorate(BufferVertexConsumer consumer, Consumer<BufferVertexConsumer> runDecorated) {
        runDecorated.accept(new DecoratedConsumer(consumer, this));
    }

    public void decorate(BufferBuilder buf, Consumer<BufferBuilder> runDecorated) {
        runDecorated.accept(new DecoratedBufferBuilder(buf, this));
    }

    public VertexConsumer decorate(VertexConsumer builder) {
        return new DecoratedBuilder(builder, this);
    }

    public BufferVertexConsumer decorate(BufferVertexConsumer builder) {
        return new DecoratedConsumer(builder, this);
    }

    public BufferBuilder decorate(BufferBuilder builder) {
        return new DecoratedBufferBuilder(builder, this);
    }

    private static class DecoratedBuilder implements VertexConsumer {

        final VertexConsumer vertexBuilder;
        final BufferDecoratorBuilder decorator;

        private DecoratedBuilder(VertexConsumer vertexBuilder, BufferDecoratorBuilder decorator) {
            this.vertexBuilder = vertexBuilder;
            this.decorator = decorator;
        }

        ///////////////////////////////////////////////////////////////////////////
        //      Methods with decorated changes
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            if (this.decorator.positionDecorator != null) {
                double[] newPosition = this.decorator.positionDecorator.decorate(x, y, z);
                this.vertexBuilder.vertex(newPosition[0], newPosition[1], newPosition[2]);
                return this;
            }
            this.vertexBuilder.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            if (this.decorator.colorDecorator != null) {
                int[] newColor = this.decorator.colorDecorator.decorate(red, green, blue, alpha);
                this.vertexBuilder.color(newColor[0], newColor[1], newColor[2], newColor[3]);
                return this;
            }
            this.vertexBuilder.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            if (this.decorator.uvDecorator != null) {
                float[] newUV = this.decorator.uvDecorator.decorate(u, v);
                this.vertexBuilder.uv(newUV[0], newUV[1]);
                return this;
            }
            this.vertexBuilder.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            if (this.decorator.overlayDecorator != null) {
                int[] newUV = this.decorator.overlayDecorator.decorate(u, v);
                this.vertexBuilder.overlayCoords(newUV[0], newUV[1]);
                return this;
            }
            this.vertexBuilder.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            if (this.decorator.lightmapDecorator != null) {
                int[] newUV = this.decorator.lightmapDecorator.decorate(u, v);
                this.vertexBuilder.uv2(newUV[0], newUV[1]);
                return this;
            }
            this.vertexBuilder.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            if (this.decorator.normalDecorator != null) {
                float[] newNormals = this.decorator.normalDecorator.decorate(x, y, z);
                this.vertexBuilder.normal(newNormals[0], newNormals[1], newNormals[2]);
                return this;
            }
            this.vertexBuilder.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.vertexBuilder.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            this.vertexBuilder.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            this.vertexBuilder.unsetDefaultColor();
        }


    }

    private static class DecoratedConsumer extends DecoratedBuilder implements BufferVertexConsumer {

        final BufferVertexConsumer vertexConsumer;

        private DecoratedConsumer(BufferVertexConsumer vertexConsumer, BufferDecoratorBuilder decorator) {
            super(vertexConsumer, decorator);
            this.vertexConsumer = vertexConsumer;
        }

        ///////////////////////////////////////////////////////////////////////////
        //      Delegate decorations
        //      At this time we applied decorations ideally
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public VertexFormatElement currentElement() {
            return this.vertexConsumer.currentElement();
        }

        @Override
        public void nextElement() {
            this.vertexConsumer.nextElement();
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

    //Well, gotta do it all over again for this one..
    private static class DecoratedBufferBuilder extends BufferBuilder {

        private BufferBuilder decorated;
        private BufferDecoratorBuilder decorator;
        private DecoratedConsumer decoratedDelegate;

        public DecoratedBufferBuilder(BufferBuilder decorated, BufferDecoratorBuilder decorator) {
            super(0);
            this.decorated = decorated;
            this.decoratedDelegate = new DecoratedConsumer(this.decorated, decorator);
            this.decorator = decorator;
        }

        @Override
        public void setQuadSortOrigin(float x, float y, float z) {
            this.decorated.setQuadSortOrigin(x, y, z);
        }

        @Override
        public SortState getSortState() {
            return this.decorated.getSortState();
        }

        @Override
        public void restoreSortState(SortState state) {
            this.decorated.restoreSortState(state);
        }

        @Override
        public void begin(VertexFormat.Mode mode, VertexFormat format) {
            this.decorated.begin(mode, format);
        }

        @Override
        public boolean isCurrentBatchEmpty() {
            return this.decorated.isCurrentBatchEmpty();
        }

        @Nullable
        @Override
        public RenderedBuffer endOrDiscardIfEmpty() {
            return this.decorated.endOrDiscardIfEmpty();
        }

        @Override
        public BufferBuilder.RenderedBuffer end() {
            return this.decorated.end();
        }

        @Override
        public boolean building() {
            return this.decorated.building();
        }

        @Override
        public void clear() {
            this.decorated.clear();
        }

        @Override
        public void discard() {
            this.decorated.discard();
        }

        @Override
        public void endVertex() {
            this.decorated.endVertex();
        }

        //TODO re-check on this. we might need to unpack & repack data here
        @Override
        public void putBulkData(ByteBuffer buffer) {
            this.decorated.putBulkData(buffer);
        }

        @Override
        public void vertex(float x, float y, float z,
                              float red, float green, float blue, float alpha,
                              float texU, float texV,
                              int overlayUV, int lightmapUV,
                              float normalX, float normalY, float normalZ) {
            if (this.fastFormat) { //In normal format, the vertices get modified in their individual vertex element calls
                if (this.decorator.positionDecorator != null) {
                    double[] newPosition = this.decorator.positionDecorator.decorate(x, y, z);
                    x = (float) newPosition[0];
                    y = (float) newPosition[1];
                    z = (float) newPosition[2];
                }
                if (this.decorator.colorDecorator != null) {
                    int[] newColors = this.decorator.colorDecorator.decorate((int) red * 255, (int) green * 255, (int) blue * 255, (int) alpha * 255);
                    red   = newColors[0] / 255F;
                    green = newColors[1] / 255F;
                    blue  = newColors[2] / 255F;
                    alpha = newColors[3] / 255F;
                }
                if (this.decorator.uvDecorator != null) {
                    float[] newUV = this.decorator.uvDecorator.decorate(texU, texV);
                    texU = newUV[0];
                    texV = newUV[1];
                }
                if (this.decorator.overlayDecorator != null) {
                    int[] newOverlayCoords = this.decorator.overlayDecorator.decorate(overlayUV & 0xFFFF, (overlayUV >> 16) & 0xFFFF);
                    overlayUV = newOverlayCoords[0] | (newOverlayCoords[1] << 16);
                }
                if (this.decorator.lightmapDecorator != null) {
                    int[] newLightMapCoords = this.decorator.lightmapDecorator.decorate(lightmapUV & 0xFFFF, (lightmapUV >> 16) & 0xFFFF);
                    lightmapUV = newLightMapCoords[0] | (newLightMapCoords[1] << 16);
                }
                if (this.decorator.normalDecorator != null) {
                    float[] newNormals = this.decorator.normalDecorator.decorate(normalX, normalY, normalZ);
                    normalX = newNormals[0];
                    normalY = newNormals[1];
                    normalZ = newNormals[2];
                }
            }
            super.vertex(x, y, z, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
        }

        @Override
        public VertexFormatElement currentElement() {
            return this.decoratedDelegate.currentElement();
        }

        @Override
        public void nextElement() {
            this.decoratedDelegate.nextElement();
        }

        @Override
        public void putByte(int i, byte b) {
            this.decoratedDelegate.putByte(i, b);
        }

        @Override
        public void putFloat(int i, float f) {
            this.decoratedDelegate.putFloat(i, f);
        }

        @Override
        public void putShort(int i, short s) {
            this.decoratedDelegate.putShort(i, s);
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            this.decorated.defaultColor(red, green, blue, alpha);
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            return this.decoratedDelegate.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this.decoratedDelegate.color(red, green, blue, alpha);
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this.decoratedDelegate.uv(u, v);
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this.decoratedDelegate.overlayCoords(u, v);
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this.decoratedDelegate.uv2(u, v);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this.decoratedDelegate.normal(x, y, z);
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
