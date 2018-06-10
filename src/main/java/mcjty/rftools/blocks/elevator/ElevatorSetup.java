package mcjty.rftools.blocks.elevator;

import mcjty.lib.builder.BlockFlags;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ElevatorSetup {

    public static GenericBlock<ElevatorTileEntity, GenericContainer> elevatorBlock;

    public static void init() {
        elevatorBlock = ModBlocks.builderFactory.<ElevatorTileEntity> builder("elevator")
                .tileEntityClass(ElevatorTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .emptyContainer()
                .infusable()
                .guiId(RFTools.GUI_ELEVATOR)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.elevator")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        elevatorBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorTileEntity.class, new ElevatorTESR());
        elevatorBlock.setGuiFactory(GuiElevator::new);
    }
}
