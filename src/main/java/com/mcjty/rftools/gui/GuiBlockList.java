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
    protected int xSize = 320;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;

//    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack.png");
    private static final ResourceLocation iconLocationLeft = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack_left.png");
    private static final ResourceLocation iconLocationRight = new ResourceLocation(RFTools.MODID, "textures/gui/networkMonitorBack_right.png");
    public static final int TEXT_COLOR = 0x19979f;
    public static final int SEL_TEXT_COLOR = 0x092020;


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
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        this.mc.getTextureManager().bindTexture(iconLocationLeft);
//        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(k, l, 0, 0, 256, this.ySize);
        this.mc.getTextureManager().bindTexture(iconLocationRight);
        this.drawTexturedModalRect(k+256, l, 0, 0, this.xSize-256, this.ySize);

//        this.drawVerticalLine(k + 15, l, l + 100, 0x334455);

        int y = 0;
        HashMap<Coordinate,BlockInfo> connectedBlocks = monitorItem.getConnectedBlocks();
        for (Map.Entry<Coordinate,BlockInfo> me : connectedBlocks.entrySet()) {
            BlockInfo blockInfo = me.getValue();
            Block block = blockInfo.getBlock();
            Coordinate coordinate = me.getKey();

            int energy = blockInfo.getEnergyStored();
            int maxEnergy = blockInfo.getMaxEnergyStored();
            String energyString = energy + "/" + maxEnergy;

            int yloc = l + 5 + y;
            String displayName = getReadableName(block);
            int color = getTextColor(blockInfo);
            drawString(displayName, 100, k + 5, yloc, color);
            drawString(coordinate.toString(), 75, k+110, yloc, color);
            drawString(energyString, 105, k+190, yloc, color);

            y += mc.fontRenderer.FONT_HEIGHT + 2;
        }

        this.drawGradientRect(k + xSize-20, l + 5, k + xSize-5, l + ySize - 5, 0xFFFF0000, 0xFF00FF00);
    }

    private int getTextColor(BlockInfo blockInfo) {
        int color;
        if (blockInfo.isFirst()) {
            color = SEL_TEXT_COLOR;
        } else {
            color = TEXT_COLOR;
        }
        return color;
    }

    private void drawString(String string, int maxWidth, int x, int y, int color) {
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(string, maxWidth), x, y, color);
    }

    private String getReadableName(Block block) {
        System.out.println("block.getItemIconName() = " + block.getItemIconName());
//            Item itemDropped = Item.getItemFromBlock(block);
        Item itemDropped = block.getItemDropped(0, new Random(), 0);
        System.out.println("itemDropped.getUnlocalizedName() = " + itemDropped.getUnlocalizedName());
        System.out.println("new ItemStack(itemDropped).getDisplayName() = " + new ItemStack(itemDropped).getDisplayName());

//            String lname = block.getLocalizedName();
//            System.out.println("I18n = " + I18n.format(lname));
//            System.out.println("Stat = " + StatCollector.translateToLocal(lname));


        ItemStack s = new ItemStack(block, 1, 0);
        String displayName = s.getDisplayName();
        if (displayName.startsWith("tile.")) {
            displayName = displayName.substring(5);
        }
        if (displayName.endsWith(".name")) {
            displayName = displayName.substring(0, displayName.length()-5);
        }
        return displayName;
    }
}
