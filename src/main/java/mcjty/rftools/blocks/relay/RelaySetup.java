package mcjty.rftools.blocks.relay;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;


public class RelaySetup {
    public static BaseBlock relayBlock;


    @ObjectHolder("rftools:relay")
    public static TileEntityType<?> TYPE_RELAY;

    public static void init() {
        relayBlock = new BaseBlock("relay", new BlockBuilder()
                .tileEntitySupplier(RelayTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK)
//                .emptyContainer()
//                .guiId(GuiProxy.GUI_RELAY)
//                .property(RelayTileEntity.ENABLED)    // @todo 1.14
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.relay"));
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        relayBlock.initModel();
//        relayBlock.setGuiFactory(GuiRelay::new);
//    }
}
