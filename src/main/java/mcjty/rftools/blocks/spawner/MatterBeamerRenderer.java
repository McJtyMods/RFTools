package mcjty.rftools.blocks.spawner;

import mcjty.lib.gui.RenderHelper;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.render.DefaultISBRH;
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
        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        Tessellator tessellator = Tessellator.instance;

        MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) tileEntity;
        Coordinate destination = matterBeamerTileEntity.getDestination();
        if (destination != null) {
            int meta = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
            if ((meta & BlockTools.MASK_REDSTONE) != 0) {
                tessellator.startDrawingQuads();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                tessellator.setColorRGBA(255, 255, 255, 128);
                tessellator.setBrightness(240);
                GL11.glPushMatrix();
                this.bindTexture(redglow);

                Minecraft mc = Minecraft.getMinecraft();
                EntityClientPlayerMP p = mc.thePlayer;
                double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * f;
                double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * f;
                double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * f;
                GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

                RenderHelper.Vector start = new RenderHelper.Vector(tileEntity.xCoord + .5f, tileEntity.yCoord + .5f, tileEntity.zCoord + .5f);
                RenderHelper.Vector end = new RenderHelper.Vector(destination.getX() + .5f, destination.getY() + .5f, destination.getZ() + .5f);
                RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY, (float) doubleZ);
                RenderHelper.drawBeam(start, end, player, (meta & 1) != 0 ? .1f : .05f);

                tessellator.draw();
                GL11.glPopMatrix();
            }
        }

        Coordinate coord = new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
        if (coord.equals(RFTools.instance.clientInfo.getSelectedTE())) {
            txt = redglow;
        } else if (coord.equals(RFTools.instance.clientInfo.getDestinationTE())) {
            txt = blueglow;
        } else {
            txt = null;
        }

        if (txt != null) {
            this.bindTexture(txt);

            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA(255, 255, 255, 128);
            tessellator.setBrightness(240);

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
        }

        if (!blending) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}

