package mcjty.rftools.blocks.spawner;

import mcjty.lib.gui.RenderGlowEffect;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.tools.MinecraftTools;
import mcjty.rftools.RFTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class MatterBeamerRenderer extends TileEntitySpecialRenderer<MatterBeamerTileEntity> {

    private static final ResourceLocation redglow = new ResourceLocation(RFTools.MODID, "textures/blocks/redglow.png");
    private static final ResourceLocation blueglow = new ResourceLocation(RFTools.MODID, "textures/blocks/blueglow.png");

    @Override
    public void renderTileEntityAt(MatterBeamerTileEntity tileEntity, double x, double y, double z, float time, int destroyStage) {
        ResourceLocation txt;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        BlockPos destination = tileEntity.getDestination();
        if (destination != null) {
            if (tileEntity.isPowered()) {
                GlStateManager.pushMatrix();

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

                Minecraft mc = Minecraft.getMinecraft();
                EntityPlayerSP p = MinecraftTools.getPlayer(mc);
                double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * time;
                double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * time;
                double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * time;

                RenderHelper.Vector start = new RenderHelper.Vector(tileEntity.getPos().getX() + .5f, tileEntity.getPos().getY() + .5f, tileEntity.getPos().getZ() + .5f);
                RenderHelper.Vector end = new RenderHelper.Vector(destination.getX() + .5f, destination.getY() + .5f, destination.getZ() + .5f);
                RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);
                GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

                this.bindTexture(redglow);

                RenderHelper.drawBeam(start, end, player, tileEntity.isGlowing() ? .1f : .05f);

                tessellator.draw();
                GlStateManager.popMatrix();
            }
        }

        BlockPos coord = tileEntity.getPos();
        if (coord.equals(RFTools.instance.clientInfo.getSelectedTE())) {
            txt = redglow;
        } else if (coord.equals(RFTools.instance.clientInfo.getDestinationTE())) {
            txt = blueglow;
        } else {
            txt = null;
        }

        if (txt != null) {
            this.bindTexture(txt);
            RenderGlowEffect.renderGlow(tessellator, x, y, z);
        }
    }
}

