package com.mcjty.rftools.blocks.screens;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ScreenRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation texture = new ResourceLocation(RFTools.MODID, "textures/blocks/machineSide.png");
    private final ModelScreen screenModel = new ModelScreen();

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        GL11.glPushMatrix();
        float f1 = 0.6666667F;
        float f3;

        int j = tileEntity.getBlockMetadata();
        f3 = 0.0F;

        if (j == 2) {
            f3 = 180.0F;
        }

        if (j == 4) {
            f3 = 90.0F;
        }

        if (j == 5) {
            f3 = -90.0F;
        }

        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.75F * f1, (float) z + 0.5F);
        GL11.glRotatef(-f3, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);

        this.bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(f1, -f1, -f1);
        this.screenModel.render();
        GL11.glPopMatrix();
        FontRenderer fontrenderer = this.func_147498_b();
        f3 = 0.016666668F * f1;
        GL11.glTranslatef(0.0F, 0.5F * f1, 0.07F * f1);
        GL11.glScalef(f3, -f3, f3);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
        GL11.glDepthMask(false);
        byte b0 = 0;

        String[] test = new String[] { "This is line 1", "This is line 2" };
        for (int i = 0; i < test.length; ++i) {
            String s = test[i];

            fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - test.length * 5, b0);
        }

        GL11.glDepthMask(true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}
