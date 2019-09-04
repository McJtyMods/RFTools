package mcjty.rftools.blocks.elevator;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ElevatorSetup {

    public static BaseBlock elevatorBlock;

    @ObjectHolder("rftools:elevator")
    public static TileEntityType<?> TYPE_ELEVATOR;

    public static void init() {
        elevatorBlock = new BaseBlock("elevator", new BlockBuilder()
                .tileEntitySupplier(ElevatorTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT)
//                .rotationType(BaseBlock.RotationType.HORIZROTATION)
//                .emptyContainer()
                .infusable()
//                .guiId(GuiProxy.GUI_ELEVATOR)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.elevator")) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }

    // @todo 1.14
//    public static void initClient() {
//        elevatorBlock.initModel();
//        ElevatorTESR.register();
//        elevatorBlock.setGuiFactory(GuiElevator::new);
//    }
}
