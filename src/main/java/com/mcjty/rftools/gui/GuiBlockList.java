package com.mcjty.rftools.gui;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.BlockInfo;
import com.mcjty.rftools.items.Coordinate;
import com.mcjty.rftools.items.NetworkMonitorItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuiBlockList extends GuiScreen {
    private NetworkMonitorItem monitorItem;

    /** The X size of the window in pixels. */
    protected int xSize = 256;
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
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(iconLocation);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

//        this.drawVerticalLine(k + 15, l, l + 100, 0x334455);

        int y = 0;
        HashMap<Coordinate,BlockInfo> connectedBlocks = monitorItem.getConnectedBlocks();
        for (Map.Entry<Coordinate,BlockInfo> me : connectedBlocks.entrySet()) {
            Block block = me.getValue().getBlock();
            Coordinate coordinate = me.getKey();

            System.out.println("block.getItemIconName() = " + block.getItemIconName());
//            Item itemDropped = Item.getItemFromBlock(block);
            Item itemDropped = block.getItemDropped(0, new Random(), 0);
            System.out.println("itemDropped.getUnlocalizedName() = " + itemDropped.getUnlocalizedName());
            System.out.println("new ItemStack(itemDropped).getDisplayName() = " + new ItemStack(itemDropped).getDisplayName());

            ItemStack s = new ItemStack(block, 1, 0);
//            String lname = block.getLocalizedName();
//            System.out.println("I18n = " + I18n.format(lname));
//            System.out.println("Stat = " + StatCollector.translateToLocal(lname));

            mc.fontRenderer.drawString(s.getDisplayName(), k + 5, l + 5 + y, 1677215);
            mc.fontRenderer.drawString(coordinate.toString(), k+160, l+5+y, 1677215);

            y += mc.fontRenderer.FONT_HEIGHT + 2;
        }

        this.drawGradientRect(k + xSize-20, l + 5, k + xSize-5, l + ySize - 5, 0xFFFF0000, 0xFF00FF00);

        mc.fontRenderer.drawString("###", k + 15, l, 0x334455);
//        this.drawVerticalLine(k+15, l, l + 50, -9408400);
    }
}
