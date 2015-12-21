package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BeamRenderer extends TileEntitySpecialRenderer<MatterTransmitterTileEntity> {
    private static final ResourceLocation textureOk = new ResourceLocation(RFTools.MODID, "textures/blocks/machineTeleporter.png");
    private static final ResourceLocation textureWarn = new ResourceLocation(RFTools.MODID, "textures/blocks/machineTeleporterWarn.png");
    private static final ResourceLocation textureUnknown = new ResourceLocation(RFTools.MODID, "textures/blocks/machineTeleporterUnknown.png");

    @Override
    public void renderTileEntityAt(MatterTransmitterTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tileEntity.isDialed() && !tileEntity.isBeamHidden()) {
            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + 1.0, z);

            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.enableDepth();

            WorldRenderer renderer = tessellator.getWorldRenderer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

//            tessellator.setBrightness(240);

            int status = tileEntity.getStatus();
            ResourceLocation beamIcon = null;
            switch (status) {
                case TeleportationTools.STATUS_OK: beamIcon = textureOk; break;
                case TeleportationTools.STATUS_WARN: beamIcon = textureWarn; break;
                default: beamIcon = textureUnknown; break;
            }
            bindTexture(beamIcon);

            long ticks = (System.currentTimeMillis() / 100) % 10;
            float i1 = ticks / 10.0f;
            float i2 = i1 + .1f;

            GlStateManager.color(1, 1, 1, 1);

            renderer.pos(0, 4, 0).tex(1, i1).endVertex();
            renderer.pos(1, 4, 0).tex(1, i2).endVertex();
            renderer.pos(1, 0, 0).tex(0, i2).endVertex();
            renderer.pos(0, 0, 0).tex(0, i1).endVertex();

            renderer.pos(1, 4, 1).tex(1, i1).endVertex();
            renderer.pos(0, 4, 1).tex(1, i2).endVertex();
            renderer.pos(0, 0, 1).tex(0, i2).endVertex();
            renderer.pos(1, 0, 1).tex(0, i1).endVertex();

            renderer.pos(0, 4, 1).tex(1, i1).endVertex();
            renderer.pos(0, 4, 0).tex(1, i2).endVertex();
            renderer.pos(0, 0, 0).tex(0, i2).endVertex();
            renderer.pos(0, 0, 1).tex(0, i1).endVertex();

            renderer.pos(1, 4, 0).tex(1, i1).endVertex();
            renderer.pos(1, 4, 1).tex(1, i2).endVertex();
            renderer.pos(1, 0, 1).tex(0, i2).endVertex();
            renderer.pos(1, 0, 0).tex(0, i1).endVertex();

            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            GlStateManager.popMatrix();
            GlStateManager.popAttrib();
        }

    }
}
