package mcjty.rftools.blocks.spaceprojector;

import mcjty.gui.Window;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.gui.widgets.WidgetList;
import mcjty.rftools.network.PacketHandler;
import mcjty.varia.BlockMeta;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiChamberDetails extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 410;
    /** The Y size of the window in pixels. */
    protected int ySize = 210;

    private static Map<BlockMeta,Integer> items = null;

    private Window window;
    private WidgetList blockList;

    public GuiChamberDetails() {
        requestChamberInfoFromServer();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static void setItemsWithCount(Map<BlockMeta,Integer> items) {
        GuiChamberDetails.items = new HashMap<BlockMeta, Integer>(items);
    }

    private void requestChamberInfoFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetChamberInfo());
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        blockList = new WidgetList(mc, this);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new HorizontalLayout()).addChild(blockList);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void populateLists() {
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        populateLists();

        window.draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }
}