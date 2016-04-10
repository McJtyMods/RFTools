package mcjty.rftools.varia;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

public class RenderHelper {

    private static final Quad[] quads = new Quad[] {
            new Quad(new Vt(0, 0, 0), new Vt(1, 0, 0), new Vt(1, 0, 1), new Vt(0, 0, 1)),       // DOWN
            new Quad(new Vt(0, 1, 1), new Vt(1, 1, 1), new Vt(1, 1, 0), new Vt(0, 1, 0)),       // UP
            new Quad(new Vt(1, 1, 0), new Vt(1, 0, 0), new Vt(0, 0, 0), new Vt(0, 1, 0)),       // NORTH
            new Quad(new Vt(1, 0, 1), new Vt(1, 1, 1), new Vt(0, 1, 1), new Vt(0, 0, 1)),       // SOUTH
            new Quad(new Vt(0, 0, 1), new Vt(0, 1, 1), new Vt(0, 1, 0), new Vt(0, 0, 0)),       // WEST
            new Quad(new Vt(1, 0, 0), new Vt(1, 1, 0), new Vt(1, 1, 1), new Vt(1, 0, 1)),       // EAST
    };

    public static void renderBillboardQuadBright(double scale) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;
        GlStateManager.pushMatrix();
        mcjty.lib.gui.RenderHelper.rotateToPlayer();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        buffer.pos(-scale, -scale, 0.0D).tex(0.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(-scale, scale, 0.0D).tex(0.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(scale, scale, 0.0D).tex(1.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(scale, -scale, 0.0D).tex(1.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public static void addSideFullTexture(VertexBuffer buffer, int side, float mult, float offset) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;
        float u1 = 0;
        float v1 = 0;
        float u2 = 1;
        float v2 = 1;
        Quad quad = quads[side];
        buffer.pos(quad.v1.x * mult + offset, quad.v1.y * mult + offset, quad.v1.z * mult + offset).tex(u1, v1).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(quad.v2.x * mult + offset, quad.v2.y * mult + offset, quad.v2.z * mult + offset).tex(u1, v2).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(quad.v3.x * mult + offset, quad.v3.y * mult + offset, quad.v3.z * mult + offset).tex(u2, v2).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(quad.v4.x * mult + offset, quad.v4.y * mult + offset, quad.v4.z * mult + offset).tex(u2, v1).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
    }

    public static void drawBeam(mcjty.lib.gui.RenderHelper.Vector S, mcjty.lib.gui.RenderHelper.Vector E, mcjty.lib.gui.RenderHelper.Vector P, float width) {
        mcjty.lib.gui.RenderHelper.Vector PS = Sub(S, P);
        mcjty.lib.gui.RenderHelper.Vector SE = Sub(E, S);
        mcjty.lib.gui.RenderHelper.Vector normal = Cross(PS, SE);
        normal = normal.normalize();
        mcjty.lib.gui.RenderHelper.Vector half = Mul(normal, width);
        mcjty.lib.gui.RenderHelper.Vector p1 = Add(S, half);
        mcjty.lib.gui.RenderHelper.Vector p2 = Sub(S, half);
        mcjty.lib.gui.RenderHelper.Vector p3 = Add(E, half);
        mcjty.lib.gui.RenderHelper.Vector p4 = Sub(E, half);
        drawQuad(Tessellator.getInstance(), p1, p3, p4, p2);
    }

    private static mcjty.lib.gui.RenderHelper.Vector Cross(mcjty.lib.gui.RenderHelper.Vector a, mcjty.lib.gui.RenderHelper.Vector b) {
        float x = a.y * b.z - a.z * b.y;
        float y = a.z * b.x - a.x * b.z;
        float z = a.x * b.y - a.y * b.x;
        return new mcjty.lib.gui.RenderHelper.Vector(x, y, z);
    }

    private static mcjty.lib.gui.RenderHelper.Vector Sub(mcjty.lib.gui.RenderHelper.Vector a, mcjty.lib.gui.RenderHelper.Vector b) {
        return new mcjty.lib.gui.RenderHelper.Vector(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    private static mcjty.lib.gui.RenderHelper.Vector Add(mcjty.lib.gui.RenderHelper.Vector a, mcjty.lib.gui.RenderHelper.Vector b) {
        return new mcjty.lib.gui.RenderHelper.Vector(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    private static mcjty.lib.gui.RenderHelper.Vector Mul(mcjty.lib.gui.RenderHelper.Vector a, float f) {
        return new mcjty.lib.gui.RenderHelper.Vector(a.x * f, a.y * f, a.z * f);
    }

    public static void drawQuad(Tessellator tessellator, mcjty.lib.gui.RenderHelper.Vector p1, mcjty.lib.gui.RenderHelper.Vector p2, mcjty.lib.gui.RenderHelper.Vector p3, mcjty.lib.gui.RenderHelper.Vector p4) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;

        VertexBuffer buffer = tessellator.getBuffer();
        buffer.pos(p1.getX(), p1.getY(), p1.getZ()).tex(0.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p2.getX(), p2.getY(), p2.getZ()).tex(1.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p3.getX(), p3.getY(), p3.getZ()).tex(1.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p4.getX(), p4.getY(), p4.getZ()).tex(0.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
    }

    private static class Vt {
        public final float x;
        public final float y;
        public final float z;

        public Vt(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Quad {
        public final Vt v1;
        public final Vt v2;
        public final Vt v3;
        public final Vt v4;

        public Quad(Vt v1, Vt v2, Vt v3, Vt v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public Quad rotate(EnumFacing direction) {
            switch (direction) {
                case NORTH: return new Quad(v4, v1, v2, v3);
                case EAST: return new Quad(v3, v4, v1, v2);
                case SOUTH: return new Quad(v2, v3, v4, v1);
                case WEST: return this;
                default: return this;
            }
        }
    }
}
