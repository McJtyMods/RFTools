package mcjty.rftools.blocks.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
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

        itemList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 232, 143));
        slider = new Slider(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(240, 5, 12, 143)).setDesiredWidth(12).setVertical().setScrollable(itemList);

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(itemList).addChild(slider);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void updateList() {
        itemList.removeChildren();

        if (!inventorySlots.getSlot(0).getHasStack()) {
            return;
        }

        ItemStack storageModule = inventorySlots.getSlot(0).getStack();
        NBTTagCompound tagCompound = storageModule.getTagCompound();
        if (tagCompound != null) {
            NBTTagList items = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < items.tagCount() ; i++) {
                NBTTagCompound tag = items.getCompoundTagAt(i);
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag);

                Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
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
