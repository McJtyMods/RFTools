package com.mcjty.rftools.gui;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.NetworkMonitorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiBlockList extends GuiScreen {
    private NetworkMonitorItem monitorItem;

    private GuiListExtended guiList;

    /** The X size of the window in pixels. */
    protected int xSize = 180;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack.png");


    public GuiBlockList(NetworkMonitorItem monitorItem) {
        this.monitorItem = monitorItem;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiList = new GuiStringList(Minecraft.getMinecraft(), xSize, ySize, 0, ySize, 16);
        int k = (this.width - this.xSize) / 2;
        guiList.setSlotXBoundsFromLeft(k);
        System.out.println("xSize = " + xSize);
        System.out.println("ySize = " + ySize);

        System.out.println("guiList.left = " + guiList.left);
        System.out.println("guiList.right = " + guiList.right);
        System.out.println("guiList.width = " + guiList.width);
        System.out.println("guiList.height = " + guiList.height);
        System.out.println("guiList.top = " + guiList.top);
        System.out.println("guiList.bottom = " + guiList.bottom);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(iconLocation);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        System.out.println("k = " + k);
        System.out.println("l = " + l);

        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        guiList.drawScreen(xSize_lo, ySize_lo, par3);
//        GuiHelper.drawPlayerModel(k + 51, l + 75, 30, (k + 51) - this.xSize_lo, (l + 75 - 50) - this.ySize_lo, this.mc.thePlayer);

    }
}
