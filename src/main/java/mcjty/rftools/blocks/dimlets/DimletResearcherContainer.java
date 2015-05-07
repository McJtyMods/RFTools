package mcjty.rftools.blocks.dimlets;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import mcjty.rftools.Achievements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimletResearcherContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_DIMLETINPUT = 0;
    public static final int SLOT_DIMLETOUTPUT = 1;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.unknownDimlet)), CONTAINER_INVENTORY, SLOT_DIMLETINPUT, 64, 24, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_DIMLETOUTPUT, 118, 24, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public DimletResearcherContainer(EntityPlayer player, DimletResearcherTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (index == SLOT_DIMLETOUTPUT) {
            Achievements.trigger(player, Achievements.researching);
        } else if (index == SLOT_DIMLETINPUT) {
            Achievements.trigger(player, Achievements.theFirstStep);
        }
        return super.slotClick(index, button, mode, player);
    }
}
