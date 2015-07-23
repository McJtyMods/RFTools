package mcjty.container;

import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.gui.widgets.WidgetList;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.gui.PacketSetGuiStyle;
import mcjty.rftools.items.manual.GuiRFToolsManual;
import mcjty.network.PacketHandler;
import mcjty.gui.GuiStyle;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiSideWindow {
    protected GuiStyle style;
    private final List<WidgetList> styledLists = new ArrayList<WidgetList>();

    protected Window sideWindow;
    private Button guiButton;
    private Button helpButton;
    private int sideLeft;
    private int sideTop;

    private int manual;
    private String manualNode;

    public GuiSideWindow(int manual, String manualNode) {
        this.manual = manual;
        this.manualNode = manualNode;
    }

    public void initGui(final Minecraft mc, GuiScreen gui, int guiLeft, int guiTop, int xSize, int ySize) {
        style = PlayerExtendedProperties.getProperties(mc.thePlayer).getPreferencesProperties().getStyle();

        helpButton = new Button(mc, gui).setText("?").setLayoutHint(new PositionalLayout.PositionalHint(1, 1, 16, 16)).
                setTooltips("Open manual").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        help(mc);
                    }
                });
        guiButton = new Button(mc, gui).setText("s").setLayoutHint(new PositionalLayout.PositionalHint(1, 19, 16, 16)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        changeStyle();
                    }
                });
        setStyleTooltip();
        Panel sidePanel = new Panel(mc, gui).setLayout(new PositionalLayout()).addChild(guiButton).addChild(helpButton);
        sideLeft = guiLeft + xSize;
        sideTop = guiTop + (ySize - 20) / 2 - 8;
        sidePanel.setBounds(new Rectangle(sideLeft, sideTop, 20, 40));
        sideWindow = new Window(gui, sidePanel);
    }

    private void help(Minecraft mc) {
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

    private void updateStyle(WidgetList list) {
        if (style == GuiStyle.STYLE_BEVEL) {
            list.setFilledRectThickness(1);
        } else {
            list.setFilledBackground(GeneralConfiguration.itemListBackground);
        }
    }


    public void register(WidgetList list) {
        updateStyle(list);
        styledLists.add(list);
    }

    public Window getWindow() {
        return sideWindow;
    }
}
