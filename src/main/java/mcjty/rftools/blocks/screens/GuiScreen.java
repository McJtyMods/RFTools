package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.modulesclient.helper.ScreenModuleGuiBuilder;
import mcjty.rftools.blocks.screens.network.PacketModuleUpdate;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.Rectangle;

import static mcjty.rftools.blocks.screens.ScreenTileEntity.PARAM_TRUETYPE;

public class GuiScreen  extends GenericGuiContainer<ScreenTileEntity> {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 224;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/screen.png");

    private Panel toplevel;
    private ToggleButton buttons[] = new ToggleButton[ScreenContainer.SCREEN_MODULES];
    private Panel modulePanels[] = new Panel[ScreenContainer.SCREEN_MODULES];
    private IClientScreenModule<?>[] clientScreenModules = new IClientScreenModule<?>[ScreenContainer.SCREEN_MODULES];

    private ToggleButton bright;
    private ChoiceLabel trueType;

    private int selected = -1;

    public GuiScreen(CreativeScreenTileEntity te, ScreenContainer container) {
        this((ScreenTileEntity) te, container);
    }

    public GuiScreen(ScreenTileEntity screenTileEntity, ScreenContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, screenTileEntity, container, RFTools.GUI_MANUAL_MAIN, "screens");

        xSize = SCREEN_WIDTH;
        ySize = SCREEN_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        for (int i = 0 ; i < ScreenContainer.SCREEN_MODULES ; i++) {
            buttons[i] = new ToggleButton(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(30, 7 + i * 18 + 1, 40, 16)).setEnabled(false).setTooltips("Open the gui for this", "module");
            final int finalI = i;
            buttons[i].addButtonEvent(parent -> selectPanel(finalI));
            toplevel.addChild(buttons[i]);
            modulePanels[i] = null;
            clientScreenModules[i] = null;
        }

        bright = new ToggleButton(mc, this)
                .setName("bright")
                .setText("Bright")
                .setCheckMarker(true)
                .setTooltips("Toggle full brightness")
                .setLayoutHint(85, 123, 55, 14);
//        .setLayoutHint(7, 208, 63, 14);
        toplevel.addChild(bright);

        toplevel.addChild(new Label(mc, this).setText("Font:").setHorizontalAlignment(HorizontalAlignment.ALIGN_RIGHT).setLayoutHint(new PositionalLayout.PositionalHint(85+50+9, 123, 30, 14)));
        trueType = new ChoiceLabel(mc, this)
                .addChoices("Default", "Truetype", "Vanilla")
                .setTooltips("Set truetype font mode", "for the screen")
                .setLayoutHint(new PositionalLayout.PositionalHint(85+50+14+30, 123, 68, 14));
        int trueTypeMode = tileEntity.getTrueTypeMode();
        trueType.setChoice(trueTypeMode == 0 ? "Default" : (trueTypeMode == -1 ? "Vanilla" : "Truetype"));
        trueType.addChoiceEvent((a, b) -> sendServerCommand(RFToolsMessages.INSTANCE, ScreenTileEntity.CMD_SETTRUETYPE,
                TypedMap.builder().put(PARAM_TRUETYPE, getCurrentTruetypeChoice()).build()));
        toplevel.addChild(trueType);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        window.bind(RFToolsMessages.INSTANCE, "bright", tileEntity, ScreenTileEntity.VALUE_BRIGHT.getName());

        Keyboard.enableRepeatEvents(true);

        selected = -1;
    }

    private int getCurrentTruetypeChoice() {
        String c = trueType.getCurrentChoice();
        if ("Default".equals(c)) {
            return 0;
        }
        if ("Truetype".equals(c)) {
            return 1;
        }
        return -1;
    }

    private void selectPanel(int i) {
        if (buttons[i].isPressed()) {
            selected = i;
        } else {
            selected = -1;
        }
    }

    private void refreshButtons() {
        for (int i = 0 ; i < ScreenContainer.SCREEN_MODULES ; i++) {
            final ItemStack slot = tileEntity.getStackInSlot(i);
            if (!slot.isEmpty() && ScreenBlock.hasModuleProvider(slot)) {
                IModuleProvider moduleProvider = ScreenBlock.getModuleProvider(slot);
                Class<? extends IClientScreenModule> clientScreenModuleClass = moduleProvider.getClientScreenModule();
                if (!clientScreenModuleClass.isInstance(clientScreenModules[i])) {
                    installModuleGui(i, slot, moduleProvider, clientScreenModuleClass);
                }
            } else {
                uninstallModuleGui(i);
            }
            if (modulePanels[i] != null) {
                modulePanels[i].setVisible(selected == i);
                buttons[i].setPressed(selected == i);
            }
        }
    }

    private void uninstallModuleGui(int i) {
        buttons[i].setEnabled(false);
        buttons[i].setPressed(false);
        buttons[i].setText("");
        clientScreenModules[i] = null;
        toplevel.removeChild(modulePanels[i]);
        modulePanels[i] = null;
        if (selected == i) {
            selected = -1;
        }
    }

    private void installModuleGui(int i, final ItemStack slot, IModuleProvider moduleProvider, Class<? extends IClientScreenModule> clientScreenModuleClass) {
        buttons[i].setEnabled(true);
        toplevel.removeChild(modulePanels[i]);
        try {
            IClientScreenModule<?> clientScreenModule = clientScreenModuleClass.newInstance();
            clientScreenModules[i] = clientScreenModule;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        NBTTagCompound tagCompound = slot.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }

        final NBTTagCompound finalTagCompound = tagCompound;
        final int finalI = i;
        ScreenModuleGuiBuilder guiBuilder = new ScreenModuleGuiBuilder(mc, this, tagCompound, () -> {
            slot.setTagCompound(finalTagCompound);
            tileEntity.setInventorySlotContents(finalI, slot);
            RFToolsMessages.INSTANCE.sendToServer(new PacketModuleUpdate(tileEntity.getPos(), finalI, finalTagCompound));
        });
        clientScreenModules[i].createGui(guiBuilder);
        modulePanels[i] = guiBuilder.build();
        modulePanels[i].setLayoutHint(80, 8, 170, 114);
        modulePanels[i].setFilledRectThickness(-2).setFilledBackground(0xff8b8b8b);

        toplevel.addChild(modulePanels[i]);
        buttons[i].setText(moduleProvider.getName());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        refreshButtons();
        drawWindow();
    }
}
