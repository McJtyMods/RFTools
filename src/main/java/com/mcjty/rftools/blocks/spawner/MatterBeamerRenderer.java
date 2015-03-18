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
            drawBeam(f, new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), destination);
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

    private void drawBeam(float partialTicks, Coordinate c1, Coordinate c2) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP p = mc.thePlayer;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

        this.bindTexture(redglow);

        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA(255, 255, 255, 128);
        tessellator.setBrightness(240);
        GL11.glLineWidth(10);

        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        float mx1 = c1.getX() + .5f;
        float my1 = c1.getY() + .5f;
        float mz1 = c1.getZ() + .5f;

        float mx2 = c2.getX() + .5f;
        float my2 = c2.getY() + .5f;
        float mz2 = c2.getZ() + .5f;

        tessellator.addVertex(mx1, my1, mz1);
        tessellator.addVertex(mx2, my2, mz2);

        tessellator.draw();

        if (!blending) {
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glPopMatrix();
    }
}
