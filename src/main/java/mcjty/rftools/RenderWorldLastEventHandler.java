package mcjty.rftools;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.rftools.items.smartwrench.SmartWrenchMode;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Set;

@SideOnly(Side.CLIENT)
public class RenderWorldLastEventHandler {


    public static void tick(RenderWorldLastEvent evt) {
        renderHilightedBlock(evt);
        renderProtectedBlocks(evt);
    }

    private static void renderProtectedBlocks(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP p = mc.thePlayer;
        ItemStack heldItem = p.getHeldItem();
        if (heldItem == null) {
            return;
        }
        if (heldItem.getItem() == ModItems.smartWrenchItem) {
            if (SmartWrenchItem.getCurrentMode(heldItem) == SmartWrenchMode.MODE_SELECT) {
                GlobalCoordinate current = SmartWrenchItem.getCurrentBlock(heldItem);
                if (current != null) {
                    if (current.getDimension() == mc.theWorld.provider.dimensionId) {
                        TileEntity te = mc.theWorld.getTileEntity(current.getCoordinate().getX(), current.getCoordinate().getY(), current.getCoordinate().getZ());
                        if (te instanceof BlockProtectorTileEntity) {
                            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                            Set<Coordinate> coordinates = blockProtectorTileEntity.getProtectedBlocks();
                            if (!coordinates.isEmpty()) {
                                renderProtectionBlocks(evt, p, new Coordinate(te.xCoord, te.yCoord, te.zCoord), coordinates);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void renderProtectionBlocks(RenderWorldLastEvent evt, EntityClientPlayerMP p, Coordinate base, Set<Coordinate> coordinates) {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.partialTicks;

        GL11.glPushMatrix();
        GL11.glColor3ub((byte) 255, (byte) 0, (byte) 0);
        GL11.glLineWidth(3);
        GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        boolean txt2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        Tessellator tessellerator = Tessellator.instance;
        tessellerator.startDrawing(GL11.GL_LINES);

        for (Coordinate coordinate : coordinates) {
            renderProtectionBlock(tessellerator, base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ());
        }

        tessellerator.draw();

        if (depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        if (txt2D) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();
    }

    private static void renderProtectionBlock(Tessellator tessellerator, float mx, float my, float mz) {
        tessellerator.addVertex(mx, my, mz);
        tessellerator.addVertex(mx+1, my, mz);
        tessellerator.addVertex(mx, my, mz);
        tessellerator.addVertex(mx, my+1, mz);
        tessellerator.addVertex(mx, my, mz);
        tessellerator.addVertex(mx, my, mz+1);
        tessellerator.addVertex(mx+1, my+1, mz+1);
        tessellerator.addVertex(mx, my+1, mz+1);
        tessellerator.addVertex(mx+1, my+1, mz+1);
        tessellerator.addVertex(mx+1, my, mz+1);
        tessellerator.addVertex(mx+1, my+1, mz+1);
        tessellerator.addVertex(mx+1, my+1, mz);

        tessellerator.addVertex(mx, my+1, mz);
        tessellerator.addVertex(mx, my+1, mz+1);
        tessellerator.addVertex(mx, my+1, mz);
        tessellerator.addVertex(mx+1, my+1, mz);

        tessellerator.addVertex(mx+1, my, mz);
        tessellerator.addVertex(mx+1, my, mz+1);
        tessellerator.addVertex(mx+1, my, mz);
        tessellerator.addVertex(mx+1, my+1, mz);

        tessellerator.addVertex(mx, my, mz+1);
        tessellerator.addVertex(mx+1, my, mz+1);
        tessellerator.addVertex(mx, my, mz+1);
        tessellerator.addVertex(mx, my+1, mz+1);
    }

    private static void renderHilightedBlock(RenderWorldLastEvent evt) {
        Coordinate c = RFTools.instance.clientInfo.getHilightedBlock();
        if (c == null) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        long time = System.currentTimeMillis();

        if (time > RFTools.instance.clientInfo.getExpireHilight()) {
            RFTools.instance.clientInfo.hilightBlock(null, -1);
            return;
        }

        if (((time / 500) & 1) == 0) {
            return;
        }

        EntityClientPlayerMP p = mc.thePlayer;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.partialTicks;

        GL11.glPushMatrix();
        GL11.glColor3ub((byte)255,(byte)0,(byte)0);
        GL11.glLineWidth(3);
        GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        boolean txt2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        Tessellator tessellator = Tessellator.instance;
        float mx = c.getX();
        float my = c.getY();
        float mz = c.getZ();
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.addVertex(mx, my, mz);
        tessellator.addVertex(mx+1, my, mz);
        tessellator.addVertex(mx, my, mz);
        tessellator.addVertex(mx, my+1, mz);
        tessellator.addVertex(mx, my, mz);
        tessellator.addVertex(mx, my, mz+1);
        tessellator.addVertex(mx+1, my+1, mz+1);
        tessellator.addVertex(mx, my+1, mz+1);
        tessellator.addVertex(mx+1, my+1, mz+1);
        tessellator.addVertex(mx+1, my, mz+1);
        tessellator.addVertex(mx+1, my+1, mz+1);
        tessellator.addVertex(mx+1, my+1, mz);

        tessellator.addVertex(mx, my+1, mz);
        tessellator.addVertex(mx, my+1, mz+1);
        tessellator.addVertex(mx, my+1, mz);
        tessellator.addVertex(mx+1, my+1, mz);

        tessellator.addVertex(mx+1, my, mz);
        tessellator.addVertex(mx+1, my, mz+1);
        tessellator.addVertex(mx+1, my, mz);
        tessellator.addVertex(mx+1, my+1, mz);

        tessellator.addVertex(mx, my, mz+1);
        tessellator.addVertex(mx+1, my, mz+1);
        tessellator.addVertex(mx, my, mz+1);
        tessellator.addVertex(mx, my+1, mz+1);

        tessellator.draw();

        if (depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        if (txt2D) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();
    }
}
