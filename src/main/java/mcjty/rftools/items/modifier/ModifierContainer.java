package mcjty.rftools.items.modifier;

import mcjty.lib.container.*;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

import static mcjty.rftools.blocks.builder.BuilderSetup.CONTAINER_MODIFIER;

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

    public ModifierContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te, PlayerEntity player) {
        super(CONTAINER_MODIFIER, id, factory, pos, te);
        cardIndex = player.inventory.currentItem;
    }



    @Override
    protected Slot createSlot(SlotFactory slotFactory, IItemHandler inventory, int index, int x, int y, SlotType slotType) {
        if (slotType == SlotType.SLOT_PLAYERHOTBAR && index == cardIndex) {
            return new BaseSlot(inventories.get(slotFactory.getInventoryName()), te, slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                @Override
                public boolean canTakeStack(PlayerEntity player) {
                    // We don't want to take the stack from this slot.
                    return false;
                }
            };
        } else {
            return super.createSlot(slotFactory, inventory, index, x, y, slotType);
        }
    }


}
