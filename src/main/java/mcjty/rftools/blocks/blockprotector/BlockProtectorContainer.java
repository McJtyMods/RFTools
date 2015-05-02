package mcjty.rftools.blocks.blockprotector;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;

public class BlockProtectorContainer extends GenericContainer {
    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public BlockProtectorContainer(EntityPlayer player) {
        super(factory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
