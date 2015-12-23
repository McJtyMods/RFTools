package mcjty.rftools.blocks.crafter;

import mcjty.lib.container.*;
import mcjty.rftools.items.storage.StorageFilterItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class CrafterContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (13*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 4;
    public static final int SLOT_FILTER_MODULE = SLOT_BUFFEROUT + BUFFEROUT_SIZE;

    private final IInventory crafterBaseTE;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, SLOT_CRAFTINPUT, 193, 7, 3, 18, 3, 18);
            addSlot(new SlotDefinition(SlotType.SLOT_GHOSTOUT), CONTAINER_INVENTORY, SLOT_CRAFTOUTPUT, 193, 65);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_BUFFER, 12, 97, 13, 18, 2, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_BUFFEROUT, 31, 142, 2, 18, 2, 18);

            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, StorageFilterItem.class), CONTAINER_INVENTORY, SLOT_FILTER_MODULE, 157, 65);

            layoutPlayerInventorySlots(85, 142);
        }
    };


    public CrafterContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        this.crafterBaseTE = containerInventory;
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
            };
        }
        return super.createSlot(slotFactory, inventory, index, x, y, slotType);
    }
}
