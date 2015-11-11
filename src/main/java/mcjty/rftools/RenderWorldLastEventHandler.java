package mcjty.rftools;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.shapecard.ShapeCardItem;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.rftools.items.smartwrench.SmartWrenchMode;
import mcjty.rftools.render.DefaultISBRH;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
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
                                renderHighlightedBlocks(evt, p, new Coordinate(te.xCoord, te.yCoord, te.zCoord), coordinates);
                            }
                        }
                    }
                }
            }
        } else if (heldItem.getItem() == ModItems.shapeCardItem) {
            int mode = ShapeCardItem.getMode(heldItem);
            if (mode == ShapeCardItem.MODE_CORNER1 || mode == ShapeCardItem.MODE_CORNER2) {
                GlobalCoordinate current = ShapeCardItem.getCurrentBlock(heldItem);
                if (current != null && current.getDimension() == mc.theWorld.provider.dimensionId) {
                    Set<Coordinate> coordinates = new HashSet<Coordinate>();
                    coordinates.add(new Coordinate(0, 0, 0));
                    if (mode == ShapeCardItem.MODE_CORNER2) {
                        Coordinate cur = current.getCoordinate();
                        Coordinate c = ShapeCardItem.getCorner1(heldItem);
                        if (c != null) {
                            coordinates.add(new Coordinate(c.getX() - cur.getX(), c.getY() - cur.getY(), c.getZ() - cur.getZ()));
                        }
                    }
                    renderHighlightedBlocks(evt, p, current.getCoordinate(), coordinates);
                }
            }
        }
    }

    private static final ResourceLocation yellowglow = new ResourceLocation(RFTools.MODID, "textures/blocks/yellowglow.png");

    private static void renderHighlightedBlocks(RenderWorldLastEvent evt, EntityClientPlayerMP p, Coordinate base, Set<Coordinate> coordinates) {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        boolean txt2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        Tessellator tessellator = Tessellator.instance;


        Minecraft.getMinecraft().getTextureManager().bindTexture(yellowglow);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(255, 255, 255, 64);
        tessellator.setBrightness(240);

        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (Coordinate coordinate : coordinates) {
            float x = base.getX() + coordinate.getX();
            float y = base.getY() + coordinate.getY();
            float z = base.getZ() + coordinate.getZ();
            tessellator.addTranslation(x, y, z);

            DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.UP.ordinal(), 1.1f, -0.05f);
            DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.DOWN.ordinal(), 1.1f, -0.05f);
            DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.NORTH.ordinal(), 1.1f, -0.05f);
            DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.SOUTH.ordinal(), 1.1f, -0.05f);
            DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.WEST.ordinal(), 1.1f, -0.05f);
            DefaultISBRH.addSideFullTexture(tessellator, ForgeDirection.EAST.ordinal(), 1.1f, -0.05f);
            tessellator.addTranslation(-x, -y, -z);
//            renderProtectionBlock(tessellator, base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ());
        }
        tessellator.draw();

        if (!blending) {
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3ub((byte) 128, (byte) 90, (byte) 0);
        GL11.glLineWidth(2);


        tessellator.startDrawing(GL11.GL_LINES);
        for (Coordinate coordinate : coordinates) {
            renderHighLightedBlocksOutline(tessellator, base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ());
        }
        tessellator.draw();

        if (depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        if (txt2D) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();
    }

    private static void renderHighLightedBlocksOutline(Tessellator tessellator, float mx, float my, float mz) {
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
