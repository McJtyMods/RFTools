package com.mcjty.rftools.blocks.environmental;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class EnvironmentalControllerRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation texture = new ResourceLocation(RFTools.MODID, "textures/entities/floatingSphere.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        GL11.glPushMatrix();

        float t = (System.currentTimeMillis() % 10000) / 10000.0f;

        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.0F, (float) z + 0.5F);
        GL11.glRotatef(t * 360.0f, 0.0F, 1.0F, 0.0F);
//        GL11.glTranslatef(0.0F, -0.2500F, -0.4375F);
//        GL11.glScalef(1, -1, -1);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

        this.bindTexture(texture);

        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(240);
        tessellator.setColorOpaque(255, 255, 255);
        Random rand = new Random(31235);
        for (int i = 0 ; i < 20 ; i++) {
            float xx = rand.nextFloat() * 1.6f - .8f;
            float yy = rand.nextFloat();
            float zz = rand.nextFloat() * 1.6f - .8f;

            GL11.glPushMatrix();
            GL11.glTranslatef(xx, yy, zz);

            int rt = rand.nextInt(5000) + 6000;
            float tt = (System.currentTimeMillis() % rt) / (float)rt;
            GL11.glRotatef(tt * 360.0f, 0.0F, 1.0F, 0.0F);

            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-.16f, -.16f, 0, 0, 0);
            tessellator.addVertexWithUV( .16f, -.16f, 0, 1, 0);
            tessellator.addVertexWithUV( .16f,  .16f, 0, 1, 1);
            tessellator.addVertexWithUV(-.16f,  .16f, 0, 0, 1);

            tessellator.addVertexWithUV(-.16f,  .16f, 0, 0, 0);
            tessellator.addVertexWithUV( .16f,  .16f, 0, 1, 0);
            tessellator.addVertexWithUV( .16f, -.16f, 0, 1, 1);
            tessellator.addVertexWithUV(-.16f, -.16f, 0, 0, 1);
            tessellator.draw();
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
//        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        GL11.glPopMatrix();
    }
}
