package mcjty.rftools.blocks.elevator;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockFlags;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ElevatorSetup {

    public static BaseBlock elevatorBlock;

    @ObjectHolder("rftools:elevator")
    public static TileEntityType<?> TYPE_ELEVATOR;

    public static void init() {
        elevatorBlock = ModBlocks.builderFactory.<ElevatorTileEntity> builder("elevator")
                .tileEntityClass(ElevatorTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .emptyContainer()
                .infusable()
                .guiId(GuiProxy.GUI_ELEVATOR)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.elevator")
                .build();
    }

    public static void initClient() {
        elevatorBlock.initModel();
        ElevatorTESR.register();
        elevatorBlock.setGuiFactory(GuiElevator::new);
    }
}
