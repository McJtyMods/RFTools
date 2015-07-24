package mcjty.gui;

import mcjty.gui.widgets.WidgetList;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.util.List;

public class GuiItemScreen extends GuiScreen {
    protected Window window;
    protected int xSize;
    protected int ySize;
    protected int guiLeft;
    protected int guiTop;

    private GuiSideWindow sideWindow;

    public GuiItemScreen(int xSize, int ySize, int manual, String manualNode) {
        this.xSize = xSize;
        this.ySize = ySize;
        sideWindow = new GuiSideWindow(manual, manualNode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (this.width - xSize) / 2;
        guiTop = (this.height - ySize) / 2;
        sideWindow.initGui(mc, this, guiLeft, guiTop, xSize, ySize);
    }

    protected WidgetList createStyledList() {
        WidgetList list = new WidgetList(mc, this);
        sideWindow.register(list);
        return list;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
        sideWindow.getWindow().mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
        sideWindow.getWindow().handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
        sideWindow.getWindow().mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    public void drawWindow() {
        window.draw();
        sideWindow.getWindow().draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
        tooltips = sideWindow.getWindow().getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x - guiLeft, y - guiTop, mc.fontRenderer);
        }
    }

}
