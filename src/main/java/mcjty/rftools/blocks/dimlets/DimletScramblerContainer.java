package mcjty.rftools.blocks.dimlets;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import mcjty.rftools.Achievements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimletScramblerContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_DIMLETINPUT = 0;
    public static final int SLOT_DIMLETOUTPUT = 3;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.knownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETINPUT+0, 46, 7, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.knownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETINPUT+1, 82, 7, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.knownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETINPUT+2, 65, 42, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_DIMLETOUTPUT, 118, 24, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public DimletScramblerContainer(EntityPlayer player, DimletScramblerTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (index == SLOT_DIMLETOUTPUT) {
            Achievements.trigger(player, Achievements.scrambled);
        }
        return super.slotClick(index, button, mode, player);
    }
}
