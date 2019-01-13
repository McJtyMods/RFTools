package mcjty.rftools.blocks.crafter;

import mcjty.lib.container.*;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CrafterContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (13*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 4;
    public static final int SLOT_FILTER_MODULE = SLOT_BUFFEROUT + BUFFEROUT_SIZE;

    private final CrafterBaseTE crafterBaseTE;

    public CrafterBaseTE getCrafterTE() {
        return crafterBaseTE;
    }

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(RFTools.MODID, "gui/crafter.gui"));

    public CrafterContainer(EntityPlayer player, IInventory containerInventory) {
        super(CONTAINER_FACTORY);
        this.crafterBaseTE = (CrafterBaseTE)containerInventory; // TODO once this method is no longer reflectively called, refactor to remove this cast
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    protected Slot createSlot(SlotFactory slotFactory, IInventory inventory, int index, int x, int y, SlotType slotType) {
        if (index >= SLOT_BUFFER && index < SLOT_BUFFEROUT && slotType == SlotType.SLOT_INPUT) {
            return new BaseSlot(inventory, index, x, y) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    if (!crafterBaseTE.isItemValidForSlot(getSlotIndex(), stack)) {
                        return false;
                    }
                    return super.isItemValid(stack);
                }

                @Override
                public void onSlotChanged() {
                    crafterBaseTE.noRecipesWork = false;
                    super.onSlotChanged();
                }
            };
        } else if (index >= SLOT_BUFFEROUT && index < SLOT_FILTER_MODULE) {
            return new BaseSlot(inventory, index, x, y) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    if (!crafterBaseTE.isItemValidForSlot(getSlotIndex(), stack)) {
                        return false;
                    }
                    return super.isItemValid(stack);
                }

                @Override
                public void onSlotChanged() {
                    crafterBaseTE.noRecipesWork = false;
                    super.onSlotChanged();
                }
            };
        }
        return super.createSlot(slotFactory, inventory, index, x, y, slotType);
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {
        // Allow replacing input slot ghost items by shift-clicking.
        if (mode == ClickType.QUICK_MOVE &&
            index >= CrafterContainer.SLOT_BUFFER &&
            index < CrafterContainer.SLOT_BUFFEROUT) {

            int offset = index - CrafterContainer.SLOT_BUFFER;
            ItemStackList ghostSlots = crafterBaseTE.getGhostSlots();
            ItemStack ghostSlot = ghostSlots.get(offset);
            ItemStack clickedWith = player.inventory.getItemStack();
            if (!ghostSlot.isEmpty() && !ghostSlot.isItemEqual(clickedWith)) {
                ItemStack copy = clickedWith.copy();
                copy.setCount(1);
                ghostSlots.set(offset, copy);
                detectAndSendChanges();
                return ItemStack.EMPTY;
            }
        }

        return super.slotClick(index, button, mode, player);
    }
}
