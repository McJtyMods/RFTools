package com.mcjty.rftools.blocks.screens;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.screens.modulesclient.*;
import com.mcjty.rftools.blocks.screens.network.PacketGetScreenData;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ScreenRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation texture = new ResourceLocation(RFTools.MODID, "textures/blocks/screenFrame.png");
    private final ModelScreen screenModel = new ModelScreen();

    private static long lastTime = 0;

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        if (System.currentTimeMillis() - lastTime > 500) {
            lastTime = System.currentTimeMillis();
            PacketHandler.INSTANCE.sendToServer(new PacketGetScreenData(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
        }

        Map<Integer,String> screenData = SimpleScreenTileEntity.screenData.get(new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
        if (screenData == null) {
            screenData = new HashMap<Integer, String>();
        }

        GL11.glPushMatrix();
        float f3;

        int meta = tileEntity.getBlockMetadata();
        f3 = 0.0F;

        if (meta == 2) {
            f3 = 180.0F;
        }

        if (meta == 4) {
            f3 = 90.0F;
        }

        if (meta == 5) {
            f3 = -90.0F;
        }

        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);
        GL11.glRotatef(-f3, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, -0.2500F, -0.4375F);

        renderScreenBoard();

        FontRenderer fontrenderer = this.func_147498_b();

        ClientScreenModule.TransformMode mode = ClientScreenModule.TransformMode.NONE;
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_LIGHTING);

        List<ClientScreenModule> modules = ((SimpleScreenTileEntity)tileEntity).getClientScreenModules();
        int currenty = 7;
        int moduleIndex = 0;
        for (ClientScreenModule module : modules) {
            if (module != null) {
                if (module.getTransformMode() != mode) {
                    if (mode != ClientScreenModule.TransformMode.NONE) {
                        GL11.glPopMatrix();
                    }
                    GL11.glPushMatrix();
                    mode = module.getTransformMode();

                    switch (mode) {
                        case TEXT:
                            GL11.glTranslatef(-0.5F, 0.5F, 0.07F);
                            f3 = 0.0075F;
                            GL11.glScalef(f3, -f3, f3);
                            GL11.glNormal3f(0.0F, 0.0F, -1.0F);
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            break;
                        case ITEM:
                            f3 = 0.0075F;
                            GL11.glTranslatef(-0.5F, 0.5F, 0.07F);
                            GL11.glScalef(f3, -f3, -0.0001f);
                            break;
                        default:
                            break;
                    }
                }

                module.render(fontrenderer, currenty, screenData.get(moduleIndex));
                currenty += module.getHeight();
            }
            moduleIndex++;
        }

        if (mode != ClientScreenModule.TransformMode.NONE) {
            GL11.glPopMatrix();
        }

        GL11.glDepthMask(true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private void renderScreenBoard() {
        this.bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(1, -1, -1);
        this.screenModel.render();

        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(240);
        tessellator.setColorOpaque(0, 0, 0);
        tessellator.addVertex(-.46f, .46f, -0.08f);
        tessellator.addVertex(.46f, .46f, -0.08f);
        tessellator.addVertex(.46f, -.46f, -0.08f);
        tessellator.addVertex(-.46f, -.46f, -0.08f);
        tessellator.draw();

        GL11.glPopMatrix();
    }
}
