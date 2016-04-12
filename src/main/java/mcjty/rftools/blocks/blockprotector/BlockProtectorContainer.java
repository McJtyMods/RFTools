package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class BlockProtectorContainer extends GenericContainer {
    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public BlockProtectorContainer(EntityPlayer player, IInventory inventory) {
        super(factory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
