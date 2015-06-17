package mcjty.container;

import mcjty.entity.GenericTileEntity;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.gui.widgets.WidgetList;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.manual.GuiRFToolsManual;
import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketHandler;
import mcjty.rftools.network.PacketServerCommand;
import mcjty.rftools.playerprops.GuiStyle;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericGuiContainer<T extends GenericTileEntity> extends GuiContainer {
    protected Window window;
    protected final T tileEntity;

    protected GuiStyle style;
    private final List<WidgetList> styledLists = new ArrayList<WidgetList>();

    protected Window sideWindow;
    private Button guiButton;
    private Button helpButton;
    private int sideLeft;
    private int sideTop;

    private int manual;
    private String manualNode;

    public GenericGuiContainer(T tileEntity, Container container, int manual, String manualNode) {
        super(container);
        this.tileEntity = tileEntity;
        this.manual = manual;
        this.manualNode = manualNode;
    }

    @Override
    public void initGui() {
        super.initGui();
        style = PlayerExtendedProperties.getProperties(mc.thePlayer).getPreferencesProperties().getStyle();

        helpButton = new Button(mc, this).setText("?").setLayoutHint(new PositionalLayout.PositionalHint(1, 1, 16, 16)).
                setTooltips("Open manual").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        help();
                    }
                });
        guiButton = new Button(mc, this).setText("s").setLayoutHint(new PositionalLayout.PositionalHint(1, 19, 16, 16)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        changeStyle();
                    }
                });
        setStyleTooltip();
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).addChild(guiButton).addChild(helpButton);
        sideLeft = guiLeft + xSize;
        sideTop = guiTop + (ySize - 20) / 2 - 8;
        sidePanel.setBounds(new Rectangle(sideLeft, sideTop, 20, 40));
        sideWindow = new Window(this, sidePanel);
    }

    private void help() {
        EntityPlayer player = mc.thePlayer;
        GuiRFToolsManual.locatePage = manualNode;
        player.openGui(RFTools.instance, manual, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
    }

    private void setStyleTooltip() {
        guiButton.setTooltips("Gui style:", style.getStyle());
    }

    private void changeStyle() {
        int next = style.ordinal() + 1;
        if (next >= GuiStyle.values().length) {
            next = 0;
        }
        style = GuiStyle.values()[next];
        PacketHandler.INSTANCE.sendToServer(new PacketSetGuiStyle(style.getStyle()));

        setStyleTooltip();
        for (WidgetList list : styledLists) {
            list.setFilledRectThickness(0);
            list.setFilledBackground(-1);
            updateStyle(list);
        }
    }

    protected WidgetList createStyledList() {
        WidgetList list = new WidgetList(mc, this);
        updateStyle(list);
        styledLists.add(list);
        return list;
    }

    private void updateStyle(WidgetList list) {
        if (style == GuiStyle.STYLE_BEVEL) {
            list.setFilledRectThickness(1);
        } else {
            list.setFilledBackground(GeneralConfiguration.itemListBackground);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x - guiLeft, y - guiTop, mc.fontRenderer);
        }

        tooltips = sideWindow.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x - guiLeft, y - guiTop, mc.fontRenderer);
        }
    }

    protected void drawWindow() {
        window.draw();
        sideWindow.draw();
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
        sideWindow.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
        sideWindow.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
        sideWindow.mouseMovedOrUp(x, y, button);
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
