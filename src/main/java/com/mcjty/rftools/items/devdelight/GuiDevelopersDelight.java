package com.mcjty.rftools.items.devdelight;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiDevelopersDelight extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 410;
    /** The Y size of the window in pixels. */
    protected int ySize = 210;

    private static int selectedX;
    private static int selectedY;
    private static int selectedZ;
    private static List<String> blockClasses = null;
    private static List<String> teClasses = null;
    private static Map<String,DelightingInfoHelper.NBTDescription> nbtData = null;
    private static int server_metadata = 0;

    private Window window;
    private WidgetList blockClassList;
    private WidgetList teClassList;
    private WidgetList nbtDataList;
    private TabbedPanel tabbedPanel;
    private ChoiceLabel clientServerMode;
    private TextField metaData;

    private List<ToggleButton> pageButtons = new ArrayList<ToggleButton>();

    private boolean listsDirty = true;

    public GuiDevelopersDelight() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static void setSelected(int x, int y, int z) {
        selectedX = x;
        selectedY = y;
        selectedZ = z;
    }

    public static void setServerBlockClasses(List<String> blockClasses) {
        GuiDevelopersDelight.blockClasses = new ArrayList<String>(blockClasses);
    }

    public static void setServerTEClasses(List<String> teClasses) {
        GuiDevelopersDelight.teClasses = new ArrayList<String>(teClasses);
    }

    public static void setServerNBTData(Map<String,DelightingInfoHelper.NBTDescription> nbtData) {
        GuiDevelopersDelight.nbtData = new HashMap<String,DelightingInfoHelper.NBTDescription>(nbtData);
    }

    public static void setMetadata(int metadata) {
        server_metadata = metadata;
    }

    private void requestDelightingInfoFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetDelightingInfo(selectedX, selectedY, selectedZ));
    }

    private void requestDelightingInfoFromClient() {
        blockClasses = new ArrayList<String>();
        teClasses = new ArrayList<String>();
        nbtData = new HashMap<String, DelightingInfoHelper.NBTDescription>();

        server_metadata = DelightingInfoHelper.fillDelightingData(selectedX, selectedY, selectedZ, mc.theWorld, blockClasses, teClasses, nbtData);
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        tabbedPanel = new TabbedPanel(mc, this);
        Panel tab1 = createBlockClassesPage();
        Panel tab2 = createTeClassesPage();
        Panel tab3 = createNbtDataPage();
        tabbedPanel.addPage("Block", tab1).addPage("TE", tab2).addPage("NBT", tab3);

        ToggleButton tab1Button = createToggleButton("Block");
        ToggleButton tab2Button = createToggleButton("TE");
        ToggleButton tab3Button = createToggleButton("NBT");
        clientServerMode = new ChoiceLabel(mc, this).addChoices("Server", "Client").setChoice("Server").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                requestNewLists();
            }
        }).setDesiredHeight(16).setTooltips("Switch between client", "and server information");
        metaData = new TextField(mc, this).setDesiredHeight(14).setTooltips("Metadata for this block");

        Panel buttonPanel = new Panel(mc, this).setLayout(new VerticalLayout()).setDesiredWidth(50).addChild(tab1Button).addChild(tab2Button).addChild(tab3Button).
                addChild(new Label(mc, this).setDynamic(true)).addChild(clientServerMode).addChild(metaData).addChild(new Label(mc, this).setDynamic(true));

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new HorizontalLayout()).addChild(buttonPanel).addChild(tabbedPanel);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        requestNewLists();
    }

    private void requestNewLists() {
        listsDirty = true;
        teClasses = null;
        blockClasses = null;
        nbtData = null;
        if ("Server".equals(clientServerMode.getCurrentChoice())) {
            requestDelightingInfoFromServer();
        } else {
            requestDelightingInfoFromClient();
        }
    }

    private ToggleButton createToggleButton(final String pagename) {
        ToggleButton toggleButton = new ToggleButton(mc, this).setText(pagename).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                ToggleButton tb = (ToggleButton) parent;
                if (tb.isPressed()) {
                    activatePage(tb, pagename);
                }
            }
        }).setDynamic(true).setDesiredHeight(18);
        pageButtons.add(toggleButton);
        return toggleButton;
    }

    private void activatePage(ToggleButton tb, String pagename) {
        for (ToggleButton toggleButton : pageButtons) {
            if (tb != toggleButton) {
                toggleButton.setPressed(false);
            }
        }
        tabbedPanel.setCurrent(pagename);
    }

    private Panel createBlockClassesPage() {
        blockClassList = new WidgetList(mc, this);
        Slider listSlider = new Slider(mc, this).setDesiredWidth(12).setVertical().setScrollable(blockClassList);
        return new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(blockClassList).addChild(listSlider);
    }

    private Panel createTeClassesPage() {
        teClassList = new WidgetList(mc, this);
        Slider listSlider = new Slider(mc, this).setDesiredWidth(12).setVertical().setScrollable(teClassList);
        return new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(teClassList).addChild(listSlider);
    }

    private Panel createNbtDataPage() {
        nbtDataList = new WidgetList(mc, this);
        Slider listSlider = new Slider(mc, this).setDesiredWidth(12).setVertical().setScrollable(nbtDataList);
        return new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(nbtDataList).addChild(listSlider);
    }

    private void populateLists() {
        if (!listsDirty) {
            return;
        }
        if (teClasses == null || blockClasses == null || nbtData == null) {
            return;
        }

        listsDirty = false;

        blockClassList.removeChildren();

        Block block = Minecraft.getMinecraft().theWorld.getBlock(selectedX, selectedY, selectedZ);

        blockClassList.addChild(new Label(mc, this).setText("Loc Name: " + block.getLocalizedName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        blockClassList.addChild(new Label(mc, this).setText("Unloc Name: " + block.getUnlocalizedName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        blockClassList.addChild(new Label(mc, this).setText("Icon Name: " + block.getItemIconName()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        blockClassList.addChild(new Label(mc, this).setText("Block Name: " + Block.blockRegistry.getNameForObject(block)).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));

        for (String c : blockClasses) {
            blockClassList.addChild(new Label(mc, this).setText("Class: " + c).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        }

        teClassList.removeChildren();
        for (String c : teClasses) {
            teClassList.addChild(new Label(mc, this).setText(c).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        }

        nbtDataList.removeChildren();
        for (Map.Entry<String,DelightingInfoHelper.NBTDescription> me : nbtData.entrySet()) {
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(mc, this).setText(me.getKey()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(70));
            DelightingInfoHelper.NBTDescription value = me.getValue();
            panel.addChild(new Label(mc, this).setText(value.getType()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(50));
            panel.addChild(new Label(mc, this).setText(value.getValue()).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
            nbtDataList.addChild(panel);
        }

        metaData.setText(String.valueOf(server_metadata));
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
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }
}