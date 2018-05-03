package mcjty.rftools.blocks.relay;

import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RelaySetup {
    public static GenericBlock<RelayTileEntity, GenericContainer> relayBlock;

    public static void init() {
        relayBlock = ModBlocks.builderFactory.<RelayTileEntity> builder("relay")
                .tileEntityClass(RelayTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK)
                .emptyContainer()
                .guiId(RFTools.GUI_RELAY)
                .property(RelayTileEntity.ENABLED)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.relay")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        relayBlock.initModel();
        relayBlock.setGuiClass(GuiRelay.class);
    }
}
