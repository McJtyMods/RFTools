package com.mcjty.rftools;

import com.mcjty.rftools.render.ModRenderers;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ModRenderers.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent evt) {
        renderHilightedBlock(evt);
//        renderBuffs(evt);
    }

    private void renderBuffs(RenderWorldLastEvent evt) {
        // @todo: not yet working!
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        PlayerExtendedProperties properties = PlayerExtendedProperties.getProperties(player);
        Map<PlayerBuff, Integer> buffs = properties.getBuffs();
        if (buffs.isEmpty()) {
            return;
        }

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString("BUFF", 4, 4, 0x0066ff);
    }

    private void renderHilightedBlock(RenderWorldLastEvent evt) {
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

        Tessellator tesselerator = Tessellator.instance;
        float mx = c.getX();
        float my = c.getY();
        float mz = c.getZ();
        tesselerator.startDrawing(GL11.GL_LINES);
        tesselerator.addVertex(mx, my, mz);
        tesselerator.addVertex(mx+1, my, mz);
        tesselerator.addVertex(mx, my, mz);
        tesselerator.addVertex(mx, my+1, mz);
        tesselerator.addVertex(mx, my, mz);
        tesselerator.addVertex(mx, my, mz+1);
        tesselerator.addVertex(mx+1, my+1, mz+1);
        tesselerator.addVertex(mx, my+1, mz+1);
        tesselerator.addVertex(mx+1, my+1, mz+1);
        tesselerator.addVertex(mx+1, my, mz+1);
        tesselerator.addVertex(mx+1, my+1, mz+1);
        tesselerator.addVertex(mx+1, my+1, mz);

        tesselerator.addVertex(mx, my+1, mz);
        tesselerator.addVertex(mx, my+1, mz+1);
        tesselerator.addVertex(mx, my+1, mz);
        tesselerator.addVertex(mx+1, my+1, mz);

        tesselerator.addVertex(mx+1, my, mz);
        tesselerator.addVertex(mx+1, my, mz+1);
        tesselerator.addVertex(mx+1, my, mz);
        tesselerator.addVertex(mx+1, my+1, mz);

        tesselerator.addVertex(mx, my, mz+1);
        tesselerator.addVertex(mx+1, my, mz+1);
        tesselerator.addVertex(mx, my, mz+1);
        tesselerator.addVertex(mx, my+1, mz+1);

        tesselerator.draw();

        if (depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        if (txt2D) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();
    }
}
