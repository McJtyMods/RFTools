package mcjty.rftools.blocks.dimlets;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftools.Achievements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimensionEnscriberContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_DIMLETS = 0;
    public static final int SIZE_DIMLETS = 13*7;
    public static final int SLOT_TAB = SLOT_DIMLETS + SIZE_DIMLETS;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.knownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETS, 13, 7, 13, 18, 7, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_TAB, 13, 142, 1, 18, 1, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };

    public DimensionEnscriberContainer(EntityPlayer player, DimensionEnscriberTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (index == SLOT_TAB) {
            Achievements.trigger(player, Achievements.firstDimension);
        }
        return super.slotClick(index, button, mode, player);
    }
}
