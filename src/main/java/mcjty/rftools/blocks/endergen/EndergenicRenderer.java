package mcjty.rftools.blocks.endergen;

import mcjty.lib.gui.RenderGlowEffect;
import mcjty.lib.gui.RenderHelper;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class EndergenicRenderer extends TileEntitySpecialRenderer<EndergenicTileEntity> {

    private ResourceLocation halo = new ResourceLocation(RFTools.MODID, "textures/entities/floatingPearl.png");
    private ResourceLocation whiteflash = new ResourceLocation(RFTools.MODID, "textures/entities/whiteflash.png");
    private ResourceLocation blackflash = new ResourceLocation(RFTools.MODID, "textures/entities/redflash.png");

    private static final ResourceLocation redglow = new ResourceLocation(RFTools.MODID, "textures/blocks/redglow.png");
    private static final ResourceLocation blueglow = new ResourceLocation(RFTools.MODID, "textures/blocks/blueglow.png");

    @Override
    public void renderTileEntityAt(EndergenicTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        Tessellator tessellator = Tessellator.getInstance();
        BlockPos coord = tileEntity.getPos();
        if (coord.equals(RFTools.instance.clientInfo.getSelectedTE())) {
            bindTexture(redglow);
            RenderGlowEffect.renderGlow(tessellator, x, y, z);
        } else if (coord.equals(RFTools.instance.clientInfo.getDestinationTE())) {
            bindTexture(blueglow);
            RenderGlowEffect.renderGlow(tessellator, x, y, z);
        }

        GlStateManager.pushAttrib();

//        if (te.isActive()) {
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GlStateManager.disableAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        this.bindTexture(halo);
        float s = (System.currentTimeMillis() % 1000) / 1000.0f;
        if (s > 0.5f) {
            s = 1.0f - s;
        }
        RenderHelper.renderBillboardQuadBright(0.2f + s * 0.3f);// + random.nextFloat() * .05f);

//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        if (tileEntity.getGoodCounter() > 0) {
            this.bindTexture(whiteflash);
            RenderHelper.renderBillboardQuadBright(0.8f * (tileEntity.getGoodCounter() / 10.0f));
        }
        if (tileEntity.getBadCounter() > 0) {
            this.bindTexture(blackflash);
            RenderHelper.renderBillboardQuadBright(0.8f * (tileEntity.getBadCounter() / 20.0f));
        }

        GlStateManager.popMatrix();
//        }

        GlStateManager.popAttrib();

    }
}
