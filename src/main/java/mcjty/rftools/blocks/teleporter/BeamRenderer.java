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

    private void p(WorldRenderer renderer, int x, int y, int z, float u, float v) {
        renderer.pos(x, y, z).tex(u, v).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
    }

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
//            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.disableCull();
            GlStateManager.enableDepth();
//            GlStateManager.disableLighting();

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

            WorldRenderer renderer = tessellator.getWorldRenderer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            p(renderer, 0, 4, 0, 1, i1);
            p(renderer, 1, 4, 0, 1, i2);
            p(renderer, 1, 0, 0, 0, i2);
            p(renderer, 0, 0, 0, 0, i1);

            p(renderer, 1, 4, 1, 1, i1);
            p(renderer, 0, 4, 1, 1, i2);
            p(renderer, 0, 0, 1, 0, i2);
            p(renderer, 1, 0, 1, 0, i1);

            p(renderer, 0, 4, 1, 1, i1);
            p(renderer, 0, 4, 0, 1, i2);
            p(renderer, 0, 0, 0, 0, i2);
            p(renderer, 0, 0, 1, 0, i1);

            p(renderer, 1, 4, 0, 1, i1);
            p(renderer, 1, 4, 1, 1, i2);
            p(renderer, 1, 0, 1, 0, i2);
            p(renderer, 1, 0, 0, 0, i1);

            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            GlStateManager.popMatrix();
            GlStateManager.popAttrib();
        }

    }
}
