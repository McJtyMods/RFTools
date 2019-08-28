package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.container.GenericContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ObjectHolder;


public class BlockProtectorSetup {
    public static BlockProtectorBlock blockProtectorBlock;

    @ObjectHolder("rftools:block_protector")
    public static TileEntityType<?> TYPE_PROTECTOR;

    @ObjectHolder("rftools:block_protector")
    public static ContainerType<GenericContainer> CONTAINER_PROTECTOR;

    public static void init() {
        if(!BlockProtectorConfiguration.enabled.get()) return;
        blockProtectorBlock = new BlockProtectorBlock();
        MinecraftForge.EVENT_BUS.register(BlockProtectorEventHandlers.class);
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        if(!BlockProtectorConfiguration.enabled.get()) return;
//        blockProtectorBlock.initModel();
//    }
}
