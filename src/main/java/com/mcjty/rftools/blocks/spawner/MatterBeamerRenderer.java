package com.mcjty.rftools.blocks.spawner;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.render.DefaultISBRH;
import com.mcjty.varia.Coordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class MatterBeamerRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation redglow = new ResourceLocation(RFTools.MODID, "textures/blocks/redglow.png");
    private static final ResourceLocation blueglow = new ResourceLocation(RFTools.MODID, "textures/blocks/blueglow.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        ResourceLocation txt;

        MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) tileEntity;
        Coordinate destination = matterBeamerTileEntity.getDestination();
        if (destination != null) {
            int meta = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
            drawBeam(f, new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), destination, meta & 1);    // Use bit 0, bit 3 is used for redstone
        }

        Coordinate coord = new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
        if (coord.equals(RFTools.instance.clientInfo.getSelectedTE())) {
            txt = redglow;
        } else if (coord.equals(RFTools.instance.clientInfo.getDestinationTE())) {
            txt = blueglow;
        } else {
            return;
        }

        this.bindTexture(txt);

        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(255, 255, 255, 128);
        tessellator.setBrightness(240);

        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.UP.ordinal(), 1.1f, -0.05f);
        DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.DOWN.ordinal(), 1.1f, -0.05f);
        DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.NORTH.ordinal(), 1.1f, -0.05f);
        DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.SOUTH.ordinal(), 1.1f, -0.05f);
        DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.WEST.ordinal(), 1.1f, -0.05f);
        DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.EAST.ordinal(), 1.1f, -0.05f);

        tessellator.draw();
        GL11.glPopMatrix();

        if (!blending) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    private void drawBeam(float partialTicks, Coordinate c1, Coordinate c2, int meta) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP p = mc.thePlayer;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);

        this.bindTexture(redglow);
        if (meta != 0) {
            drawLine(c1, c2, .2f);
        }
        drawLine(c1, c2, .1f);

        if (!blending) {
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glPopMatrix();
    }

    private void drawLine(Coordinate c1, Coordinate c2, float width) {
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();
//        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA(255, 255, 255, 128);
        tessellator.setBrightness(240);
//        GL11.glLineWidth(width);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        float mx1 = c1.getX() + .5f;
        float my1 = c1.getY() + .5f;
        float mz1 = c1.getZ() + .5f;
        Vector start = new Vector(mx1, my1, mz1);

        float mx2 = c2.getX() + .5f;
        float my2 = c2.getY() + .5f;
        float mz2 = c2.getZ() + .5f;
        Vector end = new Vector(mx2, my2, mz2);

        Vector normal = Cross(start, end);
        normal = normal.normalize();
        Vector side = Cross(normal, Sub(end, start));
        side = side.normalize();
        Vector side2 = Mul(side, width / 2.0f);

        Vector oside = Cross(side, normal);
        Vector oside2 = Mul(oside, width / 2.0f);

        Vector p1 = Add(start, side2);
        Vector p2 = Sub(start, side2);
        Vector p3 = Add(start, oside2);
        Vector p4 = Sub(start, oside2);

        Vector p5 = Add(end, side2);
        Vector p6 = Sub(end, side2);
        Vector p7 = Add(end, oside2);
        Vector p8 = Sub(end, oside2);

        drawQuad(tessellator, p1, p2, p5, p6);
        drawQuad(tessellator, p2, p1, p6, p5);
        drawQuad(tessellator, p8, p7, p4, p3);
        drawQuad(tessellator, p7, p8, p3, p4);

        tessellator.draw();
    }

    private void drawQuad(Tessellator tessellator, Vector p1, Vector p2, Vector p3, Vector p4) {
        tessellator.addVertex(p1.getX(), p1.getY(), p1.getZ());
        tessellator.addVertex(p2.getX(), p2.getY(), p2.getZ());
        tessellator.addVertex(p3.getX(), p3.getY(), p3.getZ());
        tessellator.addVertex(p4.getX(), p4.getY(), p4.getZ());
    }

    private static class Vector {
        private final float x;
        private final float y;
        private final float z;

        private Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public float norm() {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }

        public Vector normalize() {
            float n = norm();
            return new Vector(x / n, y / n, z / n);
        }
    }

    private static Vector Cross(Vector a, Vector b) {
        float x = a.y*b.z - a.z*b.y;
        float y = a.z*b.x - a.x*b.z;
        float z = a.x*b.y - a.y*b.x;
        return new Vector(x, y, z);
    }

    private static Vector Sub(Vector a, Vector b) {
        return new Vector(a.x-b.x, a.y-b.y, a.z-b.z);
    }
    private static Vector Add(Vector a, Vector b) {
        return new Vector(a.x+b.x, a.y+b.y, a.z+b.z);
    }
    private static Vector Mul(Vector a, float f) {
        return new Vector(a.x * f, a.y * f, a.z * f);
    }

}

