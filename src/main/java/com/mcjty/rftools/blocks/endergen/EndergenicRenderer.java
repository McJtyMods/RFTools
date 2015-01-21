package com.mcjty.rftools.blocks.endergen;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.render.DefaultISBRH;
import com.mcjty.varia.Coordinate;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class EndergenicRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation redglow = new ResourceLocation(RFTools.MODID, "textures/blocks/redglow.png");
    private static final ResourceLocation blueglow = new ResourceLocation(RFTools.MODID, "textures/blocks/blueglow.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        ResourceLocation txt;

        Coordinate coord = new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
        if (coord.equals(RFTools.instance.clientInfo.getSelectedEndergenicTileEntity())) {
            txt = redglow;
        } else if (coord.equals(RFTools.instance.clientInfo.getDestinationEndergenicTileEntity())) {
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
}
