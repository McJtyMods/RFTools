package mcjty.rftools.blocks.teleporter;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

public class BeamRenderer extends TileEntityRenderer<MatterTransmitterTileEntity> {
    private static final ResourceLocation textureOk = new ResourceLocation(RFTools.MODID, "textures/blocks/machineteleporter.png");
    private static final ResourceLocation textureWarn = new ResourceLocation(RFTools.MODID, "textures/blocks/machineteleporterwarn.png");
    private static final ResourceLocation textureUnknown = new ResourceLocation(RFTools.MODID, "textures/blocks/machineteleporterunknown.png");

    private void p(BufferBuilder renderer, double x, double y, double z, double u, double v) {
        renderer.pos(x, y, z).tex(u, v).color(1.0f, 1.0f, 1.0f, 1.0f).lightmap(0, 240).endVertex();
    }

    @Override
    public void render(MatterTransmitterTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tileEntity.isDialed() && !tileEntity.isBeamHidden()) {
            Tessellator tessellator = Tessellator.getInstance();
//            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();
            GlStateManager.translated(x, y + 1.0, z);

            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.disableCull();
            GlStateManager.enableDepthTest();

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

            GlStateManager.color4f(1, 1, 1, 1);

            BufferBuilder renderer = tessellator.getBuffer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

            double o = .15;
            p(renderer, o, 4, o, 1, i1);
            p(renderer, 1-o, 4, o, 1, i2);
            p(renderer, 1-o, 0, o, 0, i2);
            p(renderer, o, 0, o, 0, i1);

            p(renderer, 1-o, 4, 1-o, 1, i1);
            p(renderer, o, 4, 1-o, 1, i2);
            p(renderer, o, 0, 1-o, 0, i2);
            p(renderer, 1-o, 0, 1-o, 0, i1);

            p(renderer, o, 4, 1-o, 1, i1);
            p(renderer, o, 4, o, 1, i2);
            p(renderer, o, 0, o, 0, i2);
            p(renderer, o, 0, 1-o, 0, i1);

            p(renderer, 1-o, 4, o, 1, i1);
            p(renderer, 1-o, 4, 1-o, 1, i2);
            p(renderer, 1-o, 0, 1-o, 0, i2);
            p(renderer, 1-o, 0, o, 0, i1);

            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            GlStateManager.popMatrix();
//            GlStateManager.popAttrib();
        }

    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(MatterTransmitterTileEntity.class, new BeamRenderer());
    }
}
