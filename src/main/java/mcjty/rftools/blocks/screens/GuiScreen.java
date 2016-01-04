package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import mcjty.rftools.blocks.screens.network.PacketModuleUpdate;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class GuiScreen  extends GenericGuiContainer<ScreenTileEntity> {
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 224;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/screen.png");

    private Panel toplevel;
    private ToggleButton buttons[] = new ToggleButton[ScreenContainer.SCREEN_MODULES];
    private Panel modulePanels[] = new Panel[ScreenContainer.SCREEN_MODULES];
    private ClientScreenModule[] clientScreenModules = new ClientScreenModule[ScreenContainer.SCREEN_MODULES];

    private int selected = -1;

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
            buttons[i].addButtonEvent(new ButtonEvent() {
                @Override
                public void buttonClicked(Widget parent) {
                    selectPanel(finalI);
                }
            });
            toplevel.addChild(buttons[i]);
            modulePanels[i] = null;
            clientScreenModules[i] = null;
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        Keyboard.enableRepeatEvents(true);

        selected = -1;
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
            if (slot != null && slot.getItem() != null && slot.getItem() instanceof ModuleProvider) {
                ModuleProvider moduleProvider = (ModuleProvider) slot.getItem();
                Class<? extends ClientScreenModule> clientScreenModuleClass = moduleProvider.getClientScreenModule();
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

    private void installModuleGui(int i, final ItemStack slot, ModuleProvider moduleProvider, Class<? extends ClientScreenModule> clientScreenModuleClass) {
        buttons[i].setEnabled(true);
        toplevel.removeChild(modulePanels[i]);
        try {
            ClientScreenModule clientScreenModule = clientScreenModuleClass.newInstance();
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
        modulePanels[i] = clientScreenModules[i].createGui(mc, this, tagCompound, new ModuleGuiChanged() {
            @Override
            public void updateData() {
                slot.setTagCompound(finalTagCompound);
                tileEntity.setInventorySlotContents(finalI, slot);
                RFToolsMessages.INSTANCE.sendToServer(new PacketModuleUpdate(tileEntity.getPos(), finalI, finalTagCompound));
            }
        });
        modulePanels[i].setLayoutHint(new PositionalLayout.PositionalHint(80, 8, 170, 130));
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
