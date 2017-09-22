package mcjty.rftools.items.modifier;

import mcjty.lib.container.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ModifierContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

	public static final int COUNT_SLOTS = 2;
	public static final int SLOT_FILTER = 0;
	public static final int SLOT_REPLACEMENT = 1;

    private int cardIndex;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
			addSlot(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_FILTER, 10, 8);
			addSlot(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_REPLACEMENT, 154, 8);
            layoutPlayerInventorySlots(10, 146);
        }
    };

    public ModifierContainer(EntityPlayer player) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, new ModifierInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        cardIndex = player.inventory.currentItem;
        generateSlots();
    }

    @Override
    protected Slot createSlot(SlotFactory slotFactory, IInventory inventory, int index, int x, int y, SlotType slotType) {
        if (slotType == SlotType.SLOT_PLAYERHOTBAR && index == cardIndex) {
            return new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                @Override
                public boolean canTakeStack(EntityPlayer player) {
                    // We don't want to take the stack from this slot.
                    return false;
                }
            };
        } else {
            return super.createSlot(slotFactory, inventory, index, x, y, slotType);
        }
    }


}
