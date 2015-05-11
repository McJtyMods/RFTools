package mcjty.rftools.blocks.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.SelectionEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.Argument;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.awt.*;


public class GuiModularStorage extends GenericGuiContainer<ModularStorageTileEntity> {
    public static final int STORAGE_WIDTH = 256;
    public static final int STORAGE_HEIGHT = 236;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/modularstorage.png");

    private WidgetList itemList;
    private Slider slider;

    public GuiModularStorage(ModularStorageTileEntity modularStorageTileEntity, ModularStorageContainer container) {
        super(modularStorageTileEntity, container);

        xSize = STORAGE_WIDTH;
        ySize = STORAGE_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        itemList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 3, 232, 147)).addSelectionEvent(new SelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                selectItem();
            }

            @Override
            public void doubleClick(Widget parent, int index) {

            }
        });
        slider = new Slider(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(240, 3, 12, 147)).setDesiredWidth(12).setVertical().setScrollable(itemList);

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(itemList).addChild(slider);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void selectItem() {
        int selected = itemList.getFirstSelected();
        if (selected == -1) {
            return;
        }
        sendServerCommand(ModularStorageTileEntity.CMD_DRAGITEM, new Argument("selected", selected));
    }

    private void updateList() {
        itemList.removeChildren();

        if (!inventorySlots.getSlot(0).getHasStack()) {
            return;
        }

        for (int i = 2 ; i < tileEntity.getSizeInventory() ; i++) {
            ItemStack stack = tileEntity.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0) {
                Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(12);
                panel.addChild(new BlockRender(mc, this).setRenderItem(stack));
                String displayName = stack != null ? stack.getDisplayName() : "";
                panel.addChild(new Label(mc, this).setText(displayName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
                //        panel.addChild(new mcjty.gui.widgets.Label(mc, this).setDynamic(true).setText(c.toString()));
                itemList.addChild(panel);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        boolean shift = (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
        if (shift) {
            Slot slot = getSlotAtPosition(x, y);
            if (slot != null) {
                if (slot.getHasStack()) {
                    ItemStack storageModule = inventorySlots.getSlot(0).getStack();
                    if (storageModule != null) {
                        sendServerCommand(ModularStorageTileEntity.CMD_SHIFTCLICK_SLOT, new Argument("slot", slot.getSlotIndex()));
                        return;
                    }
                }
            }
        }

        super.mouseClicked(x, y, button);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateList();
        window.draw();
    }
}
