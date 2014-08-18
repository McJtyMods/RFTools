package com.mcjty.rftools.gui;

import com.mcjty.gui.*;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.BlockInfo;
import com.mcjty.rftools.items.Coordinate;
import com.mcjty.rftools.items.NetworkMonitorItem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
import java.util.Map;

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

    Widget toplevel;

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

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        Label label1 = new Label(mc, this).setText("Label1").setColor(0xff0000).setDesiredWidth(50);
        Label label2 = new Label(mc, this).setText("Label2").setColor(0x00ff00).setDesiredWidth(70);
        Panel panel1 = new Panel(mc, this).addChild(label1).addChild(label2).setDesiredHeight(30);

        Slider testSlider = new Slider(mc, this).setDesiredWidth(100).setDesiredHeight(20).setHorizontal().setScrollable(new TestScrollable());

        WidgetList list = new WidgetList(mc, this).setDesiredWidth(xSize-10-20-5).setDesiredHeight(105).setRowheight(20);
        populateList(list);
        Slider listSlider = new Slider(mc, this).setDesiredWidth(20).setVertical().setScrollable(list);
        Panel panel3 = new Panel(mc, this).addChild(list).addChild(listSlider).setDesiredWidth(xSize-10).setDesiredHeight(115);

        toplevel = new Panel(mc, this).addChild(panel1).addChild(testSlider).addChild(panel3).setBackground(iconLocationLeft, iconLocationRight).setLayout(new VerticalLayout());
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));
    }

    private void populateList(WidgetList list) {
        for (int i = 0 ; i < 40 ; i++) {
            String txt = "Label: "+i;
            list.addChild(new Label(mc, this).setText(txt).setColor(0x0000ff));
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if (toplevel.getBounds().contains(x, y)) {
            toplevel.mouseClick(x, y, button);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        if (k == -1) {
            mouseMovedOrUp(x, y, k);
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        // -1 == mouse move
        if (button != -1) {
            toplevel.mouseRelease(x, y, button);
        } else {
            toplevel.mouseMove(x, y);
        }
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);


//        drawOldWay();

        toplevel.draw(0, 0);

//        for (int x = 10 ; x <= 500 ; x += 10) {
//            this.drawVerticalLine(x, 0, 300, 0x7fFFFFFF);
//        }
//
//        for (int yy = 10 ; yy <= 400 ; yy += 10) {
//            this.drawHorizontalLine(0, 500, yy, 0x7fffffff);
//        }

    }

    private void drawOldWay() {
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        this.mc.getTextureManager().bindTexture(iconLocationLeft);
//        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(k, l, 0, 0, 256, this.ySize);
        this.mc.getTextureManager().bindTexture(iconLocationRight);
        this.drawTexturedModalRect(k+256, l, 0, 0, this.xSize-256, this.ySize);

        int y = 0;
        Map<Coordinate, BlockInfo> connectedBlocks = monitorItem.getConnectedBlocks();
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

    private static class TestScrollable implements Scrollable {
        private int first = 0;

        @Override
        public int getMaximum() {
            return 100;
        }

        @Override
        public int getCountSelected() {
            return 10;
        }

        @Override
        public int getFirstSelected() {
            return first;
        }

        @Override
        public void setFirstSelected(int first) {
            System.out.println("first = " + first);
            this.first = first;
        }
    }
}
