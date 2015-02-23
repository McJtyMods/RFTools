package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BeamRenderer2 extends TileEntitySpecialRenderer {
    private static final ResourceLocation textureOk = new ResourceLocation(RFTools.MODID, "textures/blocks/machineTeleporter.png");
    private static final ResourceLocation textureWarn = new ResourceLocation(RFTools.MODID, "textures/blocks/machineTeleporterWarn.png");
    private static final ResourceLocation textureUnknown = new ResourceLocation(RFTools.MODID, "textures/blocks/machineTeleporterUnknown.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) tileEntity;
        if (matterTransmitterTileEntity.isDialed()) {
            Tessellator tessellator = Tessellator.instance;
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x, (float) y + 1.0f, (float) z);

            boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            if (!blendEnabled) {
                GL11.glEnable(GL11.GL_BLEND);
            }
//            GL11.glDepthMask(false);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            if (!depthTest) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            tessellator.setBrightness(190);
            tessellator.startDrawingQuads();

            int status = matterTransmitterTileEntity.getStatus();
            ResourceLocation beamIcon = null;
            switch (status) {
                case MatterTransmitterTileEntity.STATUS_OK: beamIcon = textureOk; break;
                case MatterTransmitterTileEntity.STATUS_WARN: beamIcon = textureWarn; break;
                default: beamIcon = textureUnknown; break;
            }
            bindTexture(beamIcon);

            long ticks = (System.currentTimeMillis() / 100) % 10;
            float i1 = ticks / 10.0f;
            float i2 = i1 + .1f;

            tessellator.addVertexWithUV(0, 4, 0, 1, i1);
            tessellator.addVertexWithUV(1, 4, 0, 1, i2);
            tessellator.addVertexWithUV(1, 0, 0, 0, i2);
            tessellator.addVertexWithUV(0, 0, 0, 0, i1);

            tessellator.addVertexWithUV(1, 4, 1, 1, i1);
            tessellator.addVertexWithUV(0, 4, 1, 1, i2);
            tessellator.addVertexWithUV(0, 0, 1, 0, i2);
            tessellator.addVertexWithUV(1, 0, 1, 0, i1);

            tessellator.addVertexWithUV(0, 4, 1, 1, i1);
            tessellator.addVertexWithUV(0, 4, 0, 1, i2);
            tessellator.addVertexWithUV(0, 0, 0, 0, i2);
            tessellator.addVertexWithUV(0, 0, 1, 0, i1);

            tessellator.addVertexWithUV(1, 4, 0, 1, i1);
            tessellator.addVertexWithUV(1, 4, 1, 1, i2);
            tessellator.addVertexWithUV(1, 0, 1, 0, i2);
            tessellator.addVertexWithUV(1, 0, 0, 0, i1);

            tessellator.draw();

            if (!blendEnabled) {
                GL11.glDisable(GL11.GL_BLEND);
            }
//            GL11.glDepthMask(true);
//        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            if (!depthTest) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            GL11.glPopMatrix();
        }

    }
}
