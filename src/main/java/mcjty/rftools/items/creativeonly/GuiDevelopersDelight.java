package mcjty.rftools.items.creativeonly;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiDevelopersDelight extends Screen {

    /** The X size of the window in pixels. */
    protected int xSize = 410;
    /** The Y size of the window in pixels. */
    protected int ySize = 210;

    private static BlockPos selected;
    private static List<String> blockClasses = null;
    private static List<String> teClasses = null;
    private static Map<String,DelightingInfoHelper.NBTDescription> nbtData = null;

    private Window window;
    private WidgetList blockClassList;
    private WidgetList teClassList;
    private WidgetList nbtDataList;
    private TabbedPanel tabbedPanel;
    private ChoiceLabel clientServerMode;

    private List<ToggleButton> pageButtons = new ArrayList<>();

    private boolean listsDirty = true;

    public GuiDevelopersDelight() {
        super(new StringTextComponent("Developers Delight"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void setSelected(BlockPos pos) {
        selected = pos;
    }

    public static void setServerBlockClasses(List<String> blockClasses) {
        GuiDevelopersDelight.blockClasses = new ArrayList<>(blockClasses);
    }

    public static void setServerTEClasses(List<String> teClasses) {
        GuiDevelopersDelight.teClasses = new ArrayList<>(teClasses);
    }

    public static void setServerNBTData(Map<String,DelightingInfoHelper.NBTDescription> nbtData) {
        GuiDevelopersDelight.nbtData = new HashMap<>(nbtData);
    }

    private void requestDelightingInfoFromServer() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetDelightingInfo(selected));
    }

    private void requestDelightingInfoFromClient() {
        blockClasses = new ArrayList<>();
        teClasses = new ArrayList<>();
        nbtData = new HashMap<>();

        DelightingInfoHelper.fillDelightingData(selected.getX(), selected.getY(), selected.getZ(), minecraft.world, blockClasses, teClasses, nbtData);
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        tabbedPanel = new TabbedPanel(minecraft, this);
        Panel tab1 = createBlockClassesPage();
        Panel tab2 = createTeClassesPage();
        Panel tab3 = createNbtDataPage();
        tabbedPanel.addPage("Block", tab1).addPage("TE", tab2).addPage("NBT", tab3);

        ToggleButton tab1Button = createToggleButton("Block");
        ToggleButton tab2Button = createToggleButton("TE");
        ToggleButton tab3Button = createToggleButton("NBT");
        clientServerMode = new ChoiceLabel(minecraft, this).setDesiredWidth(60).addChoices("Server", "Client").setChoice("Server").addChoiceEvent((parent, newChoice) -> requestNewLists()).setDesiredHeight(16).setTooltips("Switch between client", "and server information");

        Panel buttonPanel = new Panel(minecraft, this).setLayout(new VerticalLayout()).setDesiredWidth(62)
                .addChildren(tab1Button, tab2Button, tab3Button, new Label(minecraft, this).setDynamic(true), clientServerMode, new Label(minecraft, this).setDynamic(true));

        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new HorizontalLayout())
                .addChildren(buttonPanel, tabbedPanel);
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
        ToggleButton toggleButton = new ToggleButton(minecraft, this).setText(pagename).addButtonEvent(parent -> {
            ToggleButton tb = (ToggleButton) parent;
            if (tb.isPressed()) {
                activatePage(tb, pagename);
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
        blockClassList = new WidgetList(minecraft, this).setName("blocks");
        Slider listSlider = new Slider(minecraft, this).setDesiredWidth(11).setVertical().setScrollableName("blocks");
        return new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(blockClassList).addChild(listSlider);
    }

    private Panel createTeClassesPage() {
        teClassList = new WidgetList(minecraft, this).setName("classes");
        Slider listSlider = new Slider(minecraft, this).setDesiredWidth(11).setVertical().setScrollableName("classes");
        return new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(teClassList).addChild(listSlider);
    }

    private Panel createNbtDataPage() {
        nbtDataList = new WidgetList(minecraft, this).setName("nbtdata");
        Slider listSlider = new Slider(minecraft, this).setDesiredWidth(11).setVertical().setScrollableName("nbtdata");
        return new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(nbtDataList).addChild(listSlider);
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

        BlockState state = Minecraft.getInstance().world.getBlockState(selected);
        Block block = state.getBlock();

        blockClassList.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText("Loc Name: " + block.getNameTextComponent().getFormattedText()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
        blockClassList.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText("Unloc Name: " + block.getTranslationKey()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
        blockClassList.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText("Block Name: " + block.getRegistryName()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));

        for (String c : blockClasses) {
            blockClassList.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText("Class: " + c).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
        }

        teClassList.removeChildren();
        for (String c : teClasses) {
            teClassList.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText(c).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
        }

        nbtDataList.removeChildren();
        for (Map.Entry<String,DelightingInfoHelper.NBTDescription> me : nbtData.entrySet()) {
            Panel panel = new Panel(minecraft, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText(me.getKey()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setDesiredWidth(70));
            DelightingInfoHelper.NBTDescription value = me.getValue();
            panel.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText(value.getType()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setDesiredWidth(50));
            panel.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText(value.getValue()).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
            nbtDataList.addChild(panel);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean rc = super.mouseClicked(x, y, button);
        window.mouseClicked((int)x, (int)y, button);
        return rc;
    }

    // @todo 1.14
//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//        window.handleMouseInput();
//    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        boolean rc = super.mouseReleased(mouseX, mouseY, state);
        window.mouseMovedOrUp((int)mouseX, (int)mouseY, state);
        return rc;
    }

    // @todo 1.14
//    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException {
//        super.keyTyped(typedChar, keyCode);
//        window.keyTyped(typedChar, keyCode);
//    }


    @Override
    public void render(int xSize_lo, int ySize_lo, float par3) {
        super.render(xSize_lo, ySize_lo, par3);

        populateLists();

        window.draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;

            MouseHelper mouse = getMinecraft().mouseHelper;
            int x = (int)mouse.getMouseX() * width / getMinecraft().mainWindow.getWidth();
            int y = height - (int)mouse.getMouseY() * height / getMinecraft().mainWindow.getHeight() - 1;

            renderTooltip(tooltips, x-guiLeft, y-guiTop, minecraft.fontRenderer);
        }
    }
}