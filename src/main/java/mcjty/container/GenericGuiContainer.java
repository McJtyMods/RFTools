package mcjty.container;

import mcjty.entity.GenericTileEntity;
import mcjty.gui.Window;
import mcjty.gui.widgets.WidgetList;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketHandler;
import mcjty.rftools.network.PacketServerCommand;
import mcjty.rftools.playerprops.GuiStyle;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public abstract class GenericGuiContainer<T extends GenericTileEntity> extends GuiContainer {
    protected Window window;
    protected final T tileEntity;

    protected GuiStyle style;

    public GenericGuiContainer(T tileEntity, Container container) {
        super(container);
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        style = PlayerExtendedProperties.getProperties(mc.thePlayer).getPreferencesProperties().getStyle();
    }

    protected WidgetList createStyledList() {
        WidgetList list = new WidgetList(mc, this);
        if (style == GuiStyle.STYLE_EDGED) {
            list.setFilledRectThickness(1);
        } else {
            list.setFilledBackground(GeneralConfiguration.itemListBackground);
        }
        return list;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x - guiLeft, y - guiTop, mc.fontRenderer);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
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
        if (!window.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void sendServerCommand(String command, Argument... arguments) {
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                command, arguments));
    }
}
